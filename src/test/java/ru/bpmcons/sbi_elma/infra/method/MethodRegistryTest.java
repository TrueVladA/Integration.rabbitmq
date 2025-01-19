package ru.bpmcons.sbi_elma.infra.method;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import ru.bpmcons.sbi_elma.infra.message.MessageErrorHandler;
import ru.bpmcons.sbi_elma.infra.method.exception.MethodNotFoundException;
import ru.bpmcons.sbi_elma.infra.version.Since;
import ru.bpmcons.sbi_elma.infra.version.Until;
import ru.bpmcons.sbi_elma.infra.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MethodRegistryTest {

    @Test
    void shouldInvokeCorrectMethod() throws NoSuchMethodException {
        MethodRegistry registry = new MethodRegistry();
        registry.registerMethod("a", new MethodContainer(A.class.getMethod("a", String.class), new A()));
        registry.registerMethod("b", new MethodContainer(A.class.getMethod("b", String.class), new A()));
        registry.registerMethod("c", new MethodContainer(A.class.getMethod("c", String.class), new A()));
        registry.registerMethod("a", new MethodContainer(B.class.getMethod("a", String.class), new B()));
        registry.registerMethod("a", new MethodContainer(C.class.getMethod("a", String.class), new C()));
        registry.registerMethod("b", new MethodContainer(C.class.getMethod("b", String.class), new C()));

        registry.invoke("a", new Version(1, 0, 0), "a", o -> assertEquals(o, "1a"), new ErrorHandlerImpl());
        registry.invoke("b", new Version(1, 0, 0), "a", o -> assertEquals(o, "1b"), new ErrorHandlerImpl());
        registry.invoke("c", new Version(1, 0, 0), "a", o -> assertEquals(o, "1c"), new ErrorHandlerImpl());
        registry.invoke("a", new Version(2, 0, 0), "a", o -> assertEquals(o, "2a"), new ErrorHandlerImpl());
        assertThrows(MethodNotFoundException.class, () -> registry.invoke("b", new Version(2, 0, 0), "a", o -> assertEquals(o, "b1"), new ErrorHandlerImpl()));
        registry.invoke("a", new Version(3, 0, 0), "a", o -> assertEquals(o, "3a"), new ErrorHandlerImpl());
        registry.invoke("b", new Version(3, 0, 0), "a", o -> assertEquals(o, "3b"), new ErrorHandlerImpl());
    }

    public static class A {
        @Method("'a'")
        public String a(String s) {
            return "1a";
        }

        @Method("'b'")
        @Until(major = 2)
        public String b(String s) {
            return "1b";
        }

        @Method("'c'")
        public String c(String s) {
            return "1c";
        }
    }

    public static class B {
        @Method("'a'")
        @Since(major = 2)
        public String a(String s) {
            return "2a";
        }
    }

    public static class C {
        @Method("'a'")
        @Since(major = 3)
        public String a(String s) {
            return "3a";
        }

        @Method("'b'")
        @Since(major = 3)
        public String b(String s) {
            return "3b";
        }
    }

    private static class ErrorHandlerImpl implements MessageErrorHandler {

        @SneakyThrows
        @Override
        public void handleException(Exception exception, Object arg) {
            throw exception;
        }

        @SneakyThrows
        @Override
        public void logException(Exception exception, Object arg) {
            throw exception;
        }
    }
}