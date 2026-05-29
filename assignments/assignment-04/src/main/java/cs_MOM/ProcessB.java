package cs_MOM;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class ProcessB {
    private final static String QUEUE_CS_REQUEST = "CS request";
    private final static String QUEUE_CS_EXIT = "CS exit";
    private final static String MY_PRIVATE_QUEUE = "Queue_Process_B";
    private final static String NO_EXCHANGE = "";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(MY_PRIVATE_QUEUE, true, false, false, null);

        System.out.println("[Process B] Asking for permission to enter CS...");
        channel.basicPublish(NO_EXCHANGE, QUEUE_CS_REQUEST, null, MY_PRIVATE_QUEUE.getBytes(StandardCharsets.UTF_8));

        DeliverCallback permissionCallback = (consumerTag, delivery) -> {
            System.out.println("[Process B] Msg received from middleware: " + new String(delivery.getBody(), StandardCharsets.UTF_8));

            // --- BEGINNING CS ---
            System.out.println("[Process B] >>> Entered in CS at " + System.currentTimeMillis() + " <<<");
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("[Process B] <<< Leaving CS at " + System.currentTimeMillis() + " <<<");
            // --- END CS ---

            channel.basicPublish(NO_EXCHANGE, QUEUE_CS_EXIT, null, MY_PRIVATE_QUEUE.getBytes(StandardCharsets.UTF_8));

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            channel.basicCancel(consumerTag);
        };

        channel.basicConsume(MY_PRIVATE_QUEUE, false, permissionCallback, t -> {});
    }
}
