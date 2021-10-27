package sp.grw.calendar

fun <T : Any> assertNotNull(value: T?): T {
    org.junit.Assert.assertNotNull(value)
    return value ?: error("Impossible!")
}
