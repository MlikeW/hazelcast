package hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;

public class Lock implements Common {

    @Test
    public void LockTest() {
        var clients = PORTS_LIST.stream()
                .map(Common::getClientConfig)
                .map(HazelcastClient::newHazelcastClient)
                .collect(toList());

        var futures = IntStream.range(0, clients.size())
                .mapToObj(i -> runAsync(
                        () -> doJobWithLock(clients.get(i), "client " + i + " - ")
                )).collect(toList());

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .join();
    }

    private static void doJobWithLock(HazelcastInstance hazelcastInstance, String prefix) {
        var lock = hazelcastInstance.getCPSubsystem().getLock(LOCK);
        while(true) {
            lock.lock();
            try {
                System.out.println(prefix + "Lock working...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } finally {
                System.out.println(prefix + "Work done.");
                lock.unlock();
            }
        }
    }
}