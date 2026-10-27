---
summary: Prints the messages of JOID with the [JOID] prefix, information on the standard output and warnings on the error output.
---
Every warning on [the errors page](/reference/errors/) goes through `JOIDLog`.
The `warnOnce` and `devWarnOnce` methods print a message once per key for the whole run, so that a warning raised on every frame appears a single time.

Learn: [Dev tools](/use/get-started/dev-tools/).

## info(String)

Prints the message on the standard output, after `[JOID] `.

## warn(String)

Prints the message on the error output, after `[JOID] `.

## warn(String,Throwable)

Prints the message on the error output, then the stack trace of the cause.

## warnOnce(String,String)

Prints the warning the first time this key is used, and nothing for the same key afterwards.

## warnOnce(String,String,Throwable)

Prints the warning and the stack trace of the cause the first time this key is used.

## devWarnOnce(String,Supplier)

In dev mode, prints the warning the first time this key is used; outside dev mode, it neither prints nor builds the message.

## devWarnOnce(String,Supplier,Throwable)

In dev mode, prints the warning and the stack trace of the cause the first time this key is used.

## nameOf(Class)

The simple name of the class, or its full name for an anonymous class, as the messages of JOID write it.
