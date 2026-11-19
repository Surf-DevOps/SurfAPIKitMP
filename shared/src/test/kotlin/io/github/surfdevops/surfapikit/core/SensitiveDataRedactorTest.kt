package io.github.surfdevops.surfapikit.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SensitiveDataRedactorTest {

    private fun redact(line: String) = SensitiveDataRedactor.redact(line)

    @Test
    fun `mascara o cartao do create-customer v4`() {
        val body = """{"nuIccid":"8955170220487626454","nuDDD":13,"dsNome":"Teste","nuDocumento":"99999909914",""" +
            """"dsPassword":"1234","noMvno":"CARREFOUR","nuPlano":"8591","card":{"holderName":"FULANO DE TAL",""" +
            """"cardNumber":"4111111111111111","expiryMonth":"11","expiryYear":"34","cvv":"123"},"isRecurring":false}"""

        val out = redact(body)

        listOf("4111111111111111", "FULANO DE TAL", "\"123\"", "\"1234\"").forEach {
            assertFalse("vazou $it: $out", out.contains(it))
        }
        assertTrue(out.contains(""""cardNumber":"***""""))
        assertTrue(out.contains(""""expiryMonth":"***""""))
        // O que não é sensível continua legível para depurar.
        assertTrue(out.contains(""""nuIccid":"8955170220487626454""""))
        assertTrue(out.contains(""""nuPlano":"8591""""))
        assertTrue(out.contains(""""isRecurring":false"""))
    }

    @Test
    fun `mascara numeros sem aspas, espacos e nome em maiusculas`() {
        assertEquals("""{"CVV" : "***"}""", redact("""{"CVV" : 123}"""))
        assertEquals("""{"pin": "***", "a": 1}""", redact("""{"pin": 1234, "a": 1}"""))
    }

    @Test
    fun `mascara tokens da resposta de login`() {
        val out = redact("""{"accessToken":"eyJ.a.b","refreshToken":"r-1","tokenType":"Bearer"}""")
        assertEquals("""{"accessToken":"***","refreshToken":"***","tokenType":"Bearer"}""", out)
    }

    @Test
    fun `mascara aspas escapadas dentro do valor`() {
        assertEquals("""{"password":"***","x":1}""", redact("""{"password":"a\"b","x":1}"""))
    }

    @Test
    fun `mascara form-urlencoded e query string`() {
        assertEquals("email=a%40b.com&senha=***", redact("email=a%40b.com&senha=s3cr3t"))
        assertEquals(
            "--> GET https://x/api/foo?token=***&nuIccid=895",
            redact("--> GET https://x/api/foo?token=abc&nuIccid=895"),
        )
    }

    @Test
    fun `nao mexe em campos que so parecem sensiveis`() {
        val line = """{"tokenType":"Bearer","nuMsisdn":"5513905946256","noPlano":"PLANO 29.90"}"""
        assertEquals(line, redact(line))
    }

    @Test
    fun `log desligado por padrao nao cria interceptor`() {
        assertNull(SensitiveDataRedactor.interceptor(HttpLogLevel.NONE))
    }
}
