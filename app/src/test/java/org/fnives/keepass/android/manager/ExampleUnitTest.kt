package org.fnives.keepass.android.manager

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.security.DigestOutputStream
import java.security.InvalidKeyException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.ShortBufferException
import javax.crypto.spec.SecretKeySpec

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}