package org.example.opcDemo.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * @author PanYi
 */
@Slf4j
public class SimpleMqttClient {
    /**
     * 全局唯一 单例
     */
    private static IMqttAsyncClient client;

    private final MqttConnectOptions mqttConnectOptions;

    public SimpleMqttClient(MqttConnectOptions mqttConnectOptions) {
        this.mqttConnectOptions = mqttConnectOptions;
    }

    private static IMqttAsyncClient getClient() {
        return client;
    }

    private static void setClient(IMqttAsyncClient client) {
        SimpleMqttClient.client = client;
    }

    /**
     * 连接MQTT服务器
     */
    public void connect(String serverURI, String clientID) {
        IMqttAsyncClient client = null;
        try {
            client = new MqttAsyncClient(serverURI, clientID, new MemoryPersistence());
            client.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean b, String s) {
                    log.info("connect success！");
                }

                @Override
                public void connectionLost(Throwable throwable) {
                    log.error("Lost connection!!! {}", throwable.getCause().toString());
                    throwable.printStackTrace();
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    //不接收消息
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    log.info("send success ? --> {}", iMqttDeliveryToken.isComplete());
                }
            });
            try {
                client.connect(mqttConnectOptions);
                SimpleMqttClient.setClient(client);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("connect fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 发布
     *
     * @param qos      连接方式
     * @param retained 是否保留
     * @param topic    主题
     */
    public String publish(String topic, byte[] message, int qos, boolean retained) {
        if (client != null && client.isConnected()) {
            try {
                IMqttDeliveryToken token = client.publish(topic, message, qos, retained);
                token.waitForCompletion();
                log.info("Is the message sent successfully? --> {}", token.isComplete());
                return new String(token.getMessage().getPayload());
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}


