package com.example.chess;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

// Modified from https://stackoverflow.com/a/41080735
public class MouseHandler implements EventHandler<MouseEvent> {
    private final EventHandler<MouseEvent> onClickedEventHandler;
    private final EventHandler<MouseEvent> onDraggedEventHandler;
    private final EventHandler<MouseEvent> onDroppedEventHandler;

    private boolean dragging = false;

    public MouseHandler(EventHandler<MouseEvent> onClickedEventHandler, EventHandler<MouseEvent> onDraggedEventHandler,
                        EventHandler<MouseEvent> onDroppedEventHandler) {
        this.onClickedEventHandler = onClickedEventHandler;
        this.onDraggedEventHandler = onDraggedEventHandler;
        this.onDroppedEventHandler = onDroppedEventHandler;
    }

    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            dragging = false;
        }
        else if (event.getEventType() == MouseEvent.DRAG_DETECTED) {
            dragging = true;
        }
        else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            onDraggedEventHandler.handle(event);
        }
        else if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if (dragging) {
                onDroppedEventHandler.handle(event);
            } else {
                onClickedEventHandler.handle(event);
            }
        }

    }
}
