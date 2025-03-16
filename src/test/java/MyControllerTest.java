import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class MyControllerTest {

    private void function(AtomicInteger  i){
        i.incrementAndGet();
    }

    @Test
    void testSomething() {
        AtomicInteger i = new AtomicInteger (0);
        function(i);
        System.out.println(i.get());
    }
}
