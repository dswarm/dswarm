package de.avgl.dmp.controller.factories;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.glassfish.hk2.api.Factory;

public class EventBusFactory implements Factory<EventBus> {

	private final Object[] initialListeners;
	private EventBus inst = null;

	public EventBusFactory(Object... initialListeners) {

		this.initialListeners = initialListeners;
	}

	@Override
	public EventBus provide() {
		if (inst == null) {

			final ThreadPoolExecutor executor = new ThreadPoolExecutor(
					2, 10, 1, TimeUnit.MINUTES,
					new LinkedBlockingQueue<Runnable>()
			);
			final AsyncEventBus eventBus = new AsyncEventBus(executor);

			for (Object initialListener : initialListeners) {
				eventBus.register(initialListener);
			}

			inst = eventBus;
		}
		return inst;
	}

	@Override
	public void dispose(EventBus instance) {}
}
