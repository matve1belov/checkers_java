package network;

public class Session {

	/** Обработчик соединения для этого клиента. */
	private ConnectionListener listener;

	/** Идентификатор сессии, используемый для связи между двумя клиентами. */
	private String sid;

	/** Имя хоста или IP-адрес назначения. */
	private String destinationHost;

	/** Порт назначения. */
	private int destinationPort;

	/**
	 * Создает новую сессию с заданными параметрами.
	 *
	 * @param listener Обработчик соединения на этом клиенте.
	 * @param sid Идентификатор сессии.
	 * @param destinationHost Имя хоста или IP-адрес назначения.
	 * @param destinationPort Порт назначения.
	 */
	public Session(ConnectionListener listener, String sid,
			String destinationHost, int destinationPort) {
		this.listener = listener;
		this.sid = sid;
		this.destinationHost = destinationHost;
		this.destinationPort = destinationPort;
	}

	/**
	 * Создает новую сессию с заданными параметрами.
	 *
	 * @param sid Идентификатор сессии.
	 * @param sourcePort Порт источника.
	 * @param destinationHost Имя хоста или IP-адрес назначения.
	 * @param destinationPort Порт назначения.
	 */
	public Session(String sid, int sourcePort,
			String destinationHost, int destinationPort) {
		this.listener = new ConnectionListener(sourcePort);
		this.sid = sid;
		this.destinationHost = destinationHost;
		this.destinationPort = destinationPort;
	}


	/**
	 * Получает обработчик соединения на этом клиенте.
	 *
	 * @return Обработчик соединения на этом клиенте.
	 */
	public ConnectionListener getListener() {
		return listener;
	}

	/**
	 * Устанавливает обработчик соединения на этом клиенте.
	 *
	 * @param listener Обработчик соединения на этом клиенте.
	 */
	public void setListener(ConnectionListener listener) {
		this.listener = listener;
	}

	/**
	 * Получает идентификатор сессии, используемый для связи между двумя клиентами.
	 *
	 * @return Идентификатор сессии.
	 */
	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getDestinationHost() {
		return destinationHost;
	}

	public void setDestinationHost(String destinationHost) {
		this.destinationHost = destinationHost;
	}

	public int getDestinationPort() {
		return destinationPort;
	}

	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}
	
	public int getSourcePort() {
		return (listener == null? -1 : listener.getPort());
	}
	
	public void setSourcePort(int sourcePort) {
		if (listener != null) {
			this.listener.setPort(sourcePort);
		}
	}
}
