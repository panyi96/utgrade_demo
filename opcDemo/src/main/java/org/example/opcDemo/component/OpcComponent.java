package org.example.opcDemo.component;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.opcDemo.entity.ExcelData;
import org.example.opcDemo.mqtt.SimpleMqttClient;
import org.jinterop.dcom.common.JIException;
import org.jinterop.dcom.core.JIVariant;
import org.openscada.opc.lib.common.AlreadyConnectedException;
import org.openscada.opc.lib.common.ConnectionInformation;
import org.openscada.opc.lib.common.NotConnectedException;
import org.openscada.opc.lib.da.DuplicateGroupException;
import org.openscada.opc.lib.da.Group;
import org.openscada.opc.lib.da.Item;
import org.openscada.opc.lib.da.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;


/**
 * @author PanYi
 */
@Slf4j
@Component
public class OpcComponent {
    private static final List<ExcelData> EXCEL_DATA = new ArrayList<>();
    private static final Map<String, ExcelData> EXCEL_DATA_MAP = new HashMap<>();

    @Autowired
    private SimpleMqttClient simpleMqttClient;

    @PostConstruct
    public void initOpcData() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("file/opc仪表20211227.xlsx");
        // 判断文件是否存在
        boolean exists = classPathResource.exists();
        if (!exists) {
            log.error("找不到点位文件！");
            throw new Exception("找不到点位文件！");
        }
        InputStream inputStream1 = classPathResource.getInputStream();
        InputStream inputStream2 = classPathResource.getInputStream();
        List<ExcelData> list1 = EasyExcel.read(inputStream1).head(ExcelData.class).sheet(0).doReadSync();
        List<ExcelData> list2 = EasyExcel.read(inputStream2).head(ExcelData.class).sheet(1).doReadSync();
        EXCEL_DATA.addAll(list1);
        EXCEL_DATA.addAll(list2);
        log.info("读取opc点位表成功！");
    }

    /**
     * 每1小时执行一次
     * 发送数据到mqtt上
     */
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void getOpcData() {
        log.info("=======开始读取OPC数据========");
        //opc连接信息
        final ConnectionInformation ci = new ConnectionInformation();
        ci.setHost("192.168.50.32");
        ci.setUser("Administrator");
        ci.setPassword("lp123456");
        ci.setClsid("15a4274d-d1bb-4906-8f28-a1b4994662a6");
        ci.setProgId("");
        //连接对象
        final Server server = new Server(ci, Executors.newSingleThreadScheduledExecutor());
        String[] keys = new String[EXCEL_DATA.size()];
        for (int i = 0; i < EXCEL_DATA.size(); i++) {
            keys[i] = EXCEL_DATA.get(i).getKey();
            EXCEL_DATA_MAP.put(EXCEL_DATA.get(i).getKey(), EXCEL_DATA.get(i));
        }
        try {
            //建立连接
            server.connect();
            //添加一个组
            Group group = server.addGroup();
            //将标记添加到组里
            Map<String, Item> items = new HashMap<>();
            for (String key : keys) {
                String value = null;
                try {
                    Item item = group.addItem(key);
                    JIVariant jiVariant = item.read(false).getValue();
                    value = jiVariant.getObject().toString();
                } catch (Exception e) {
                    log.error("====<{}>读取失败====", key);
                }
                EXCEL_DATA_MAP.get(key).setValue(value);
            }
            simpleMqttClient.publish("/iot/wfsOpc/gateway/realtimeData", JSON.toJSONBytes(EXCEL_DATA), 0, false);
            log.info("====读取opc数据完毕====");
            log.info(JSON.toJSONString(EXCEL_DATA));
        } catch (UnknownHostException | AlreadyConnectedException | JIException |
                NotConnectedException | DuplicateGroupException e) {
            e.printStackTrace();
            log.error("====读取opc数据发生异常！====");
        } finally {
            //关闭连接
            server.disconnect();
        }
    }
}

