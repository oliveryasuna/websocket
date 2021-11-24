/*
 * Copyright 2021 Oliver Yasuna
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without
 *      specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.oliveryasuna.websocket;

import com.oliveryasuna.commons.language.pattern.Registration;
import com.oliveryasuna.websocket.observable.event.*;
import com.oliveryasuna.websocket.observable.listener.WebSocketListener;
import org.springframework.web.socket.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ObservableWebSocketHandler extends AbstractWebSocketHandler {

  @SuppressWarnings("rawtypes")
  private final Map<Class<? extends WebSocketEvent>, Set<WebSocketListener>> listeners = new HashMap<>();

  public ObservableWebSocketHandler() {
    super();
  }

  @Override
  public final void afterConnectionEstablished(final WebSocketSession session) throws Exception {
    fireEvent(new WebSocketConnectedEvent(session));
  }

  @Override
  public final void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) throws Exception {
    fireEvent(new WebSocketClosedEvent(session, status));
  }

  @Override
  protected final void handleTextMessage(final WebSocketSession session, final TextMessage message) throws Exception {
    fireEvent(new WebSocketTextMessageEvent(session, message));
  }

  @Override
  protected final void handleBinaryMessage(final WebSocketSession session, final BinaryMessage message) throws Exception {
    fireEvent(new WebSocketBinaryMessageEvent(session, message));
  }

  @Override
  protected final void handlePongMessage(final WebSocketSession session, final PongMessage message) throws Exception {
    fireEvent(new WebSocketPongMessageEvent(session, message));
  }

  @Override
  public final void handleTransportError(final WebSocketSession session, final Throwable exception) throws Exception {
    fireEvent(new WebSocketTransportErrorEvent(session, exception));
  }

  public final Registration addConnectedListener(final WebSocketListener<WebSocketConnectedEvent> listener) {
    return addListener(WebSocketConnectedEvent.class, listener);
  }

  public final Registration addClosedListener(final WebSocketListener<WebSocketClosedEvent> listener) {
    return addListener(WebSocketClosedEvent.class, listener);
  }

  public final Registration addTextMessageListener(final WebSocketListener<WebSocketTextMessageEvent> listener) {
    return addListener(WebSocketTextMessageEvent.class, listener);
  }

  public final Registration addBinaryMessageListener(final WebSocketListener<WebSocketBinaryMessageEvent> listener) {
    return addListener(WebSocketBinaryMessageEvent.class, listener);
  }

  public final Registration addPongMessageListener(final WebSocketListener<WebSocketPongMessageEvent> listener) {
    return addListener(WebSocketPongMessageEvent.class, listener);
  }

  public final Registration addTransportErrorListener(final WebSocketListener<WebSocketTransportErrorEvent> listener) {
    return addListener(WebSocketTransportErrorEvent.class, listener);
  }

  private <E extends WebSocketEvent> Registration addListener(final Class<E> eventType, final WebSocketListener<E> listener) {
    if(eventType == null) throw new IllegalArgumentException("Argument [eventType] is null.");
    if(listener == null) throw new IllegalArgumentException("Argument [listener] is null.");

    listeners.putIfAbsent(eventType, new HashSet<>());

    if(!listeners.get(eventType).add(listener)) {
      return null;
    }

    return (() -> listeners.get(eventType).remove(listener));
  }

  @SuppressWarnings("unchecked")
  private void fireEvent(final WebSocketEvent event) {
    if(event == null) throw new IllegalArgumentException("Argument [event] is null.");

    final Class<? extends WebSocketEvent> eventType = event.getClass();

    if(!listeners.containsKey(eventType)) {
      return;
    }

    listeners.get(eventType).forEach(listener -> listener.onEvent(event));
  }

}
