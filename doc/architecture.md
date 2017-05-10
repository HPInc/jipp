# Architecture

## Design Principles

**Map types closely to IPP protocols.** This makes it easy to cross-check between the spec and implementation.

**Avoid Literals.** As tempting as it is to use a string like `"job-uri"`, there's no way to make this type-safe or
catch a misspelling.

**Prefer immutable data structures.** They work well for multithreaded use. `@AutoValue` makes them easy.

**Avoid cyclic dependencies**. This code smell leads to highly-coupled, untestable, confusing objects.

**Maintain 95%+ code coverage.** If it's not being tested on each build, you don't know if it works.

**Apply static analysis tools aggressively.** Surgeons don't permit germs to enter their sterile field for the same
reason.

**Do not rely on JDK8.** For the broadest compatibility.

**Do not get involved in HTTP/S.** Leave that for a `Transport` implementer above.

**Banish all uses of `null`.** Use guava's Optional so the compiler can tell you about problems long before your user
encounters a `NullPointerException`.

**Optimize your algorithm, not your code**. Trust the toolchain to apply any necessary optimizations.

