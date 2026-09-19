package dev.ferrox.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CoreTests {

    @Test
    void testAppError() {
        AppError err = new AppError(ErrorCode.NOT_FOUND, "Resource missing", 404);
        assertEquals(ErrorCode.NOT_FOUND, err.getCode());
        assertEquals(404, err.getStatusCode());
        assertEquals("Resource missing", err.getMessage());
        assertNull(err.getDetails());
    }

    @Test
    void testErrorResponse() {
        ErrorResponse res = new ErrorResponse(ErrorCode.UNAUTHORIZED, "Denied", 401, "extra details");
        assertNotNull(res.id());
        assertNotNull(res.timestamp());
        assertEquals(ErrorCode.UNAUTHORIZED, res.code());
        assertEquals(401, res.statusCode());
        assertEquals("Denied", res.message());
        assertEquals("extra details", res.details());
    }

    @Test
    void testPagination() {
        Pagination<String> page1 = new Pagination<>(List.of("a", "b"), 10, 1, 2);
        assertTrue(page1.hasNext());

        Pagination<String> pageLast = new Pagination<>(List.of("a", "b"), 10, 5, 2);
        assertFalse(pageLast.hasNext());
    }
}
