package lucida.main;

// Thrift java libraries 
import org.apache.thrift.server.TNonblockingServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;

// Thrift client-side code for registering w/ sirius
import org.apache.thrift.protocol.TBinaryProtocol;

// Generated code
import lucida.thrift.*;
//import lucida.thrift.cmdcenterstubs.*;

// The service handler
import lucida.handler.*;

/**
 * Starts the question-answer server and listens for requests.
 */
public class QADaemon {
	/** 
	 * An object whose methods are implementations of the question-answer thrift
	 * interface.
	 */
	public static QAServiceHandler handler;

	/**
	 * An object responsible for communication between the handler
	 * and the server. It decodes serialized data using the input protocol,
	 * delegates processing to the handler, and writes the response
	 * using the output protocol.
	 */
	public static LucidaService.Processor<QAServiceHandler> processor;

	/** 
	 * Entry point for question-answer.
	 * @param args the argument list. Provide port numbers
	 * for both sirius and qa.
	 */
	public static void main(String [] args) {
		try {
			int tmp_port = 9091;
			int tmp_cmdcenterport = 8081;
			if (args.length == 2) {
				tmp_port = Integer.parseInt(args[0].trim());
				tmp_cmdcenterport = Integer.parseInt(args[1].trim());
			} else if (args.length == 1) {
				tmp_port = Integer.parseInt(args[0].trim());
				System.out.println("Using default port for sirius: "
						+ tmp_cmdcenterport);
			} else {
				System.out.println("Using default port for qa: " + tmp_port);
//	System.out.println("Using default port for sirius: "
//						+ tmp_cmdcenterport);
			}

			// Inner classes receive copies of local variables to work with.
			// Local vars must not change to ensure that inner classes are synced.
			final int port = tmp_port;
			final int cmdcenterport = tmp_cmdcenterport;
			// The handler implements the generated java interface
			// that was originally specified in the thrift file.
			// When it's called, an Open Ephyra object is created.
			handler = new QAServiceHandler();
			processor = new LucidaService.Processor<QAServiceHandler>(handler);
			Runnable simple = new Runnable() {
				// This is the code that the thread will run
				public void run() {
					simple(processor, port, cmdcenterport);
				}
			};
			// Let system schedule the thread
			new Thread(simple).start();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	/**
	 * Listens for requests and forwards request information
	 * to handler.
	 * @param processor the thrift processor that will handle serialization
	 * and communication with the handler once a request is received.
	 * @param port the port at which the question-answer service will listen.
	 * @param cmdcenterport the port at which the sirius service is listening.
	 */
	public static void simple(LucidaService.Processor processor, final int port, final int cmdcenterport) {
		try {
			// Create a multi-threaded server: TNonblockingServer.
			TNonblockingServerTransport transport = new TNonblockingServerSocket(port);
			TNonblockingServer.Args args = new TNonblockingServer.Args(transport)
			.processor(processor)	
			.protocolFactory(new TBinaryProtocol.Factory())
			.transportFactory(new TFramedTransport.Factory());
			final TNonblockingServer server = new TNonblockingServer(args);
			server.serve();
				
			Thread t1 = new Thread(new Runnable() {
				public void run() {
					System.out.println("Starting qa at port " + port + "...");
					server.serve();
				}
			});  
			t1.start();

//      // Register this server with sirius
//      TTransport transport = new TSocket("localhost", cmdcenterport);
//      transport.open();
//      TProtocol protocol = new TBinaryProtocol(transport);
//      CommandCenter.Client client = new CommandCenter.Client(protocol);
//      System.out.println("Registering qa with sirius...");
//      MachineData mDataObj = new MachineData("localhost", port);
//      client.registerService("QA", mDataObj);
//      transport.close();

			t1.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
