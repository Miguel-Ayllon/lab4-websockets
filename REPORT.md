# Lab 4 WebSockets ELIZA

## Description of Changes

I have completed the WebSockets lab by implementing the `onChat` test in `ElizaServerTest.kt`. The primary changes are:

1.  **Removed `@Ignore` annotation** from the `onChat` test to enable it.
2.  **Implemented `ComplexClient`**: The `onMessage` function was modified to not only receive messages but also to send a reply. The client now waits to receive the `"What's on your mind?"` message and then sends the phrase `"I am feeling sad"`.
3.  **Implemented `onChat` test**:
    * A `CountDownLatch(4)` was configured to wait for the 3 greeting messages and 1 bot response.
    * The complete greeting sequence (`"The doctor is in."`, `"What's on your mind?"`, `"---"`) is verified to ensure the conversation happens in the correct order.
    * `assertTrue(size >= 4)` is used instead of `assertEquals` to make the test more robust against the asynchronous nature of WebSockets, as warned in the guide.
    * It checks that the bot's response is a valid "DOCTOR-style" reply, verifying it contains keywords related to the message sent.

---

## Technical Decisions

The most important technical decision was how to implement the reply logic in `ComplexClient`.

* **Reactive Client vs. Counter:** Instead of using brittle logic (like `if (list.size == 3)`), the client was made **reactive**. It actively waits for the specific message `"What's on your mind?"` before sending its own reply. This simulates a real conversation and makes the client robust, regardless of whether the server changes the number of greeting messages in the future.
* **Interval Assertion (`>= 4`):** I deliberately avoided `assertEquals(4, list.size)`. The guide mentions that WebSocket tests can be "intermittent". Using `assertTrue(size >= 4)` ensures that we have *at least* received the 4 expected messages from our conversation, but it doesn't fail if, due to latency, another message arrives right after, making the test more reliable.

---

## Learning Outcomes

I learned several key concepts from this assignment:

* **WebSocket Lifecycle:** I have a better understanding of the connection, message exchange, and asynchronicity of WebSockets.
* **Asynchronous Testing:** I learned how to handle the asynchronous nature of WebSockets in a test environment (JUnit) by using `CountDownLatch` to synchronize the main test thread with the WebSocket client thread.
* **Robust vs. Brittle Tests:** I understood why it's crucial to write robust assertions (like `assertTrue(size >= 4)`) instead of exact ones (`assertEquals`) when testing asynchronous systems, to avoid intermittent failures.
* **Kotlin & Jakarta WebSockets:** I reinforced my knowledge of using `@ClientEndpoint` and `@OnMessage` annotations to create WebSocket clients in Kotlin.

---

## AI Disclosure

### AI Tools Used

* None.

### AI-Assisted Work

* **Percentage of AI assistance:** 0%.
* All code and documentation were generated without AI assistance.

### Original Work

* All implementation logic in `ElizaServerTest.kt` and `ComplexClient`, as well as the writing of this `REPORT.md`, was done manually by me.