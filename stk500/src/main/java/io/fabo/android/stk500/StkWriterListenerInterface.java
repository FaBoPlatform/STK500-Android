package io.fabo.android.stk500;

import java.util.EventListener;

public interface StkWriterListenerInterface extends EventListener {

    /**
     * ステータスの変化を通知
     */
    public void onChangeStatus(int status);

    /**
     * エラーを通知
     */
    public void onError(int status);

}
