package no.nav.tms.token.support.entraid.token.verification

import java.time.Duration
import java.util.Date

operator fun Date.minus(duration: Duration): Date {

    return Date(time - duration.toMillis())
}

operator fun Date.plus(duration: Duration): Date {

    return Date(time + duration.toMillis())
}
