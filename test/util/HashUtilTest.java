package util;

import org.junit.Test;

import static org.junit.Assert.*;

public class HashUtilTest {
    // -------------------------------------------------------------------------
    // HOW JUNIT WORKS (read this once):
    //
    // Each method annotated with @Test is one test case.
    // JUnit runs them automatically when you right-click → Run 'HashUtilTest'.
    //
    // Inside each test, you use "assert" methods to check if something is true:
    //   assertEquals(expected, actual) — checks two values are equal
    //   assertTrue(condition)          — checks something is true
    //   assertFalse(condition)         — checks something is false
    //   assertNotNull(value)           — checks something is not null
    //
    // If the assertion passes → green tick
    // If it fails → red cross + tells you what went wrong
    // -------------------------------------------------------------------------

    @Test
    public void testSha256ReturnsNonNull() {
        // TODO: call HashUtil.sha256("hello") and store the result
        // Then assert that the result is not null
        // Why: basic sanity check — the method should always return something
        String password = HashUtil.sha256("hello");
        assertNotNull(password);
    }

    @Test
    public void testSha256Returns64CharHexString() {
        // TODO: call HashUtil.sha256("hello") and store the result
        // Then assert that result.length() equals 64
        // Why: SHA-256 always produces exactly 256 bits = 64 hex characters
        String password = HashUtil.sha256("hello");
        assertEquals(64, password.length());
    }

    @Test
    public void testSha256SameInputSameOutput() {
        // TODO: call HashUtil.sha256("password123") twice, store both results
        // Then assert both results are equal
        // Why: hashing is deterministic — same input must always give same output
        String actualPassword = HashUtil.sha256("password123");
        String expectedPassword = HashUtil.sha256("password123");
        assertEquals(expectedPassword, actualPassword);
    }

    @Test
    public void testSha256DifferentInputDifferentOutput() {
        // TODO: call HashUtil.sha256("abc") and HashUtil.sha256("xyz")
        // Then assert they are NOT equal (use assertNotEquals)
        // Why: different inputs must produce different hashes
        String p1 = HashUtil.sha256("abc");
        String p2 = HashUtil.sha256("xyz");
        assertNotEquals(p2, p1);
    }

    @Test
    public void testVerifyCorrectPassword() {
        // TODO: hash "myPassword" using sha256, store it
        // Then call HashUtil.verify("myPassword", thatHash)
        // Assert the result is true
        // Why: verify() should return true when the raw input matches the stored hash
        String hashPassword = HashUtil.sha256("myPassword");
        boolean passwordMatch = HashUtil.verify("myPassword", hashPassword);
        assertTrue(passwordMatch);
    }

    @Test
    public void testVerifyWrongPassword() {
        // TODO: hash "myPassword" using sha256, store it
        // Then call HashUtil.verify("wrongPassword", thatHash)
        // Assert the result is false
        // Why: verify() must reject passwords that don't match
        String hashPassword = HashUtil.sha256("myPassword");
        boolean passwordNotMatch = HashUtil.verify("wrongPassword", hashPassword);
        assertFalse(passwordNotMatch);
    }
}