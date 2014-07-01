package NioComponent.provider;

/**
 * Created by charlown on 2014/7/1.
 */
public interface INotifyOperationStateHandler {
    public void notifyOperationState(int type, int operationType, boolean isSuc);
}
