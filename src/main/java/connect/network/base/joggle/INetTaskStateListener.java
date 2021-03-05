package connect.network.base.joggle;

import connect.network.base.NetTaskStatus;

public interface INetTaskStateListener {

   void onState(NetTaskStatus status);
}
