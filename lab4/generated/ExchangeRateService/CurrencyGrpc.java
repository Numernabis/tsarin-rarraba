package ExchangeRateService;
import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.20.0)",
    comments = "Source: currency.proto")
public final class CurrencyGrpc {

  private CurrencyGrpc() {}

  public static final String SERVICE_NAME = "Currency";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<CurrencyOuterClass.SubscribedCurrencies,
      CurrencyOuterClass.CurrencyValue> getDownloadCurrenciesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DownloadCurrencies",
      requestType = CurrencyOuterClass.SubscribedCurrencies.class,
      responseType = CurrencyOuterClass.CurrencyValue.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<CurrencyOuterClass.SubscribedCurrencies,
      CurrencyOuterClass.CurrencyValue> getDownloadCurrenciesMethod() {
    io.grpc.MethodDescriptor<CurrencyOuterClass.SubscribedCurrencies, CurrencyOuterClass.CurrencyValue> getDownloadCurrenciesMethod;
    if ((getDownloadCurrenciesMethod = CurrencyGrpc.getDownloadCurrenciesMethod) == null) {
      synchronized (CurrencyGrpc.class) {
        if ((getDownloadCurrenciesMethod = CurrencyGrpc.getDownloadCurrenciesMethod) == null) {
          CurrencyGrpc.getDownloadCurrenciesMethod = getDownloadCurrenciesMethod = 
              io.grpc.MethodDescriptor.<CurrencyOuterClass.SubscribedCurrencies, CurrencyOuterClass.CurrencyValue>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "Currency", "DownloadCurrencies"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  CurrencyOuterClass.SubscribedCurrencies.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  CurrencyOuterClass.CurrencyValue.getDefaultInstance()))
                  .setSchemaDescriptor(new CurrencyMethodDescriptorSupplier("DownloadCurrencies"))
                  .build();
          }
        }
     }
     return getDownloadCurrenciesMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CurrencyStub newStub(io.grpc.Channel channel) {
    return new CurrencyStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CurrencyBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CurrencyBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CurrencyFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CurrencyFutureStub(channel);
  }

  /**
   */
  public static abstract class CurrencyImplBase implements io.grpc.BindableService {

    /**
     */
    public void downloadCurrencies(CurrencyOuterClass.SubscribedCurrencies request,
        io.grpc.stub.StreamObserver<CurrencyOuterClass.CurrencyValue> responseObserver) {
      asyncUnimplementedUnaryCall(getDownloadCurrenciesMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getDownloadCurrenciesMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                CurrencyOuterClass.SubscribedCurrencies,
                CurrencyOuterClass.CurrencyValue>(
                  this, METHODID_DOWNLOAD_CURRENCIES)))
          .build();
    }
  }

  /**
   */
  public static final class CurrencyStub extends io.grpc.stub.AbstractStub<CurrencyStub> {
    private CurrencyStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CurrencyStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CurrencyStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CurrencyStub(channel, callOptions);
    }

    /**
     */
    public void downloadCurrencies(CurrencyOuterClass.SubscribedCurrencies request,
        io.grpc.stub.StreamObserver<CurrencyOuterClass.CurrencyValue> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getDownloadCurrenciesMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CurrencyBlockingStub extends io.grpc.stub.AbstractStub<CurrencyBlockingStub> {
    private CurrencyBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CurrencyBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CurrencyBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CurrencyBlockingStub(channel, callOptions);
    }

    /**
     */
    public java.util.Iterator<CurrencyOuterClass.CurrencyValue> downloadCurrencies(
        CurrencyOuterClass.SubscribedCurrencies request) {
      return blockingServerStreamingCall(
          getChannel(), getDownloadCurrenciesMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CurrencyFutureStub extends io.grpc.stub.AbstractStub<CurrencyFutureStub> {
    private CurrencyFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CurrencyFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CurrencyFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CurrencyFutureStub(channel, callOptions);
    }
  }

  private static final int METHODID_DOWNLOAD_CURRENCIES = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CurrencyImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CurrencyImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_DOWNLOAD_CURRENCIES:
          serviceImpl.downloadCurrencies((CurrencyOuterClass.SubscribedCurrencies) request,
              (io.grpc.stub.StreamObserver<CurrencyOuterClass.CurrencyValue>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class CurrencyBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CurrencyBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return CurrencyOuterClass.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Currency");
    }
  }

  private static final class CurrencyFileDescriptorSupplier
      extends CurrencyBaseDescriptorSupplier {
    CurrencyFileDescriptorSupplier() {}
  }

  private static final class CurrencyMethodDescriptorSupplier
      extends CurrencyBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CurrencyMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (CurrencyGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CurrencyFileDescriptorSupplier())
              .addMethod(getDownloadCurrenciesMethod())
              .build();
        }
      }
    }
    return result;
  }
}
