package com.pleon.buyt

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class BuytApplicationTest {

    @Test fun `By default, the user should not be premium`() {
        assertThat(isPremium).isFalse()
    }
}
