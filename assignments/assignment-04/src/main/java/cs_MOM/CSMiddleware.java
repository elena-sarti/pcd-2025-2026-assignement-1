package cs_MOM;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class CSMiddleware {

    private final static String QUEUE_CS_REQUEST = "CS request";
    private final static String QUEUE_CS_EXIT = "CS exit";
    private final static String QUEUE_TOKEN = "token";
    private final static String NO_EXCHANGE = "";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_CS_REQUEST, true, false, false, null);
        channel.queueDeclare(QUEUE_CS_EXIT, true, false, false, null);
        channel.queueDeclare(QUEUE_TOKEN, true, false, false, null);

        // queuePurge purges the queue from tokens left by previous executions
        channel.queuePurge(QUEUE_TOKEN);
        String token = "CS token";
        channel.basicPublish(NO_EXCHANGE, QUEUE_TOKEN, null, token.getBytes(StandardCharsets.UTF_8));

        System.out.println("[*] Middleware started: waiting for processes to request permission to enter CS. To exit press CTRL+C");

        channel.basicQos(1);

        DeliverCallback csExitCallback = (processTag, delivery) -> {
            String processQueue = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[*] Process " + processQueue + " exited from CS.");
            channel.basicPublish(NO_EXCHANGE, QUEUE_TOKEN, null, token.getBytes(StandardCharsets.UTF_8));
            System.out.println("[*] No process is now in CS.");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        DeliverCallback csRequestCallback = (processTag, delivery) -> {
            String processQueue = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("[*] Received CS request by process " + processQueue);
            GetResponse tok = channel.basicGet(QUEUE_TOKEN, false);
            if (tok != null) {
                channel.queueDeclare(processQueue, true, false, false, null);
                String requestAccepted = "Permission to enter CS gained.";
                channel.basicPublish(NO_EXCHANGE, processQueue, null, requestAccepted.getBytes(StandardCharsets.UTF_8));
                System.out.println("[*] Permission to enter CS sent to " + processQueue);
                channel.basicAck(tok.getEnvelope().getDeliveryTag(), false);
            } else {
                try {
                    System.out.println("[*] CS Busy. Requeuing request from " + processQueue);
                    Thread.sleep(500);
                    channel.basicPublish(NO_EXCHANGE, QUEUE_CS_REQUEST, null, processQueue.getBytes(StandardCharsets.UTF_8));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        };

        boolean autoAck = false;
        channel.basicConsume(QUEUE_CS_REQUEST, autoAck, csRequestCallback, processTag -> {});
        channel.basicConsume(QUEUE_CS_EXIT, autoAck, csExitCallback, t -> {});
    }
}
