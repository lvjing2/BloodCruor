package com.liwn.zzl.bloodcruor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.liwn.zzl.bloodcruor.bluetooth.BluetoothChatService;
import com.liwn.zzl.bloodcruor.bluetooth.DeviceListActivity;
import com.liwn.zzl.bloodcruor.deviceConns.CommandRead;
import com.liwn.zzl.bloodcruor.deviceConns.Constants;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MarkItemFragment.OnListFragmentInteractionListener, SendFileFragment.OnFragmentInteractionListener, SettingFragment.OnFragmentInteractionListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2;
    private static final int REQUEST_CODE_TWO_PERMISSION = 3;
    private static final String PREFERENCE = "PREFERENCE";

    private Context mContext;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    private Button bluetoothStatus = null;
    private BluetoothAdapter mBluetoothAdapter = null;

    private int clickedIndex;
    private int currentIndex;
    private SendFileFragment mSendFileFragment;
    private MarkItemFragment mMarkItemFragment;
    private SettingFragment mSettingFragment;
    private Fragment[] mFragments;
    private Button[] mTabs;

    private boolean dbClickExit = false;

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
//    private SmartFragmentStatePagerAdapter mSmartFragmentStatePagerAdapter;
//    private ViewPager mViewPager;

    private String mConnectedDeviceName = null;
    private String mDeviceAddress;
    private boolean isBluetoothConnected;
    private int address;
    private File file;
    private RandomAccessFile randomAccessFile;
    private final int diff = 32;
    private byte[] sendBytes;
    private boolean isFileStartSend = false;
    private boolean isFileFinished = false;
    private boolean isFileCancled = false;
    // refresh setting or update mark libs, isUpdateType[0] is the newest type.
    private String[] isUpdateType = new String[2];
    private boolean isClickedDisConnected = false;
    private StringBuffer mOutStringBuffer;

    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private BluetoothChatService mChatService = null;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setUIStatus(true);
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setUIStatus(false);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = Constants.bytesToHexString(writeBuf);
                    Log.i(TAG, "send:  " + writeMessage);
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = Constants.bytesToHexString(readBuf);
                    Log.i(TAG, "received: " + readMessage);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != MarkBitApplication.applicationContext) {
                        Toast.makeText(MarkBitApplication.applicationContext, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != MarkBitApplication.applicationContext) {
                        Toast.makeText(MarkBitApplication.applicationContext, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private void setUIStatus(boolean isBluetoothConnected) {
        if (isBluetoothConnected) {
            bluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_bluetooth_connected_white_36dp, 0);
            bluetoothStatus.setText(mConnectedDeviceName);
            isBluetoothConnected = true;
            MarkBitApplication.connectedDeviceName = mConnectedDeviceName;
            Log.d(TAG, "broadcastReceiver connected " + mConnectedDeviceName);
            Toast.makeText(getApplicationContext(), getString(R.string.connected_to) + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
        } else {
            bluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_bluetooth_disabled_white_36dp, 0);
            bluetoothStatus.setText(R.string.display_disconnected);
            isBluetoothConnected = false;
        }
    }

    private void setUpTransation() {
        Log.d(TAG, "setUpTransation()");

        // Initialize the send button with a listener that for click events

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(MainActivity.this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_mark_management:
                clickedIndex = 0;
                break;
            case R.id.btn_draw_mark:
                clickedIndex = 1;
                break;
            case R.id.btn_send_file:
                clickedIndex = 2;
                break;
        }

        if (currentIndex != clickedIndex) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(mFragments[currentIndex]);
            if (!mFragments[clickedIndex].isAdded()) {
                ft.add(R.id.fragment_container, mFragments[clickedIndex]);
            }
            ft.show(mFragments[clickedIndex]).commit();
        }

        mTabs[currentIndex].setSelected(false);
        // 把当前tab设为选中状态
        mTabs[clickedIndex].setSelected(true);
        currentIndex = clickedIndex;
    }

    private void initView() {
        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_mark_management);
        mTabs[1] = (Button) findViewById(R.id.btn_draw_mark);
        mTabs[2] = (Button) findViewById(R.id.btn_send_file);
        mTabs[0].setSelected(true);

        mSendFileFragment = new SendFileFragment();
        mMarkItemFragment = new MarkItemFragment();
        mSettingFragment = new SettingFragment();
        mFragments = new Fragment [] {mMarkItemFragment, mSettingFragment, mSendFileFragment};

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mFragments[currentIndex]);
        if (!mFragments[clickedIndex].isAdded()) {
            ft.add(R.id.fragment_container, mFragments[clickedIndex]);
        }
        ft.show(mFragments[clickedIndex]).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!dbClickExit) {
            dbClickExit = true;
            Toast.makeText(this, getString(R.string.dbClick_Exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dbClickExit = false;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            return;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        initView();

        mContext = this;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        bluetoothStatus = (Button) findViewById(R.id.icon_bluetooth_status);
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_LONG).show();
            finish();
        }

        bluetoothStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "is connected: " + (mChatService.getState() == BluetoothChatService.STATE_CONNECTED));
                Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (mChatService == null) {
            setUpTransation();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }
//
//    @Override
//    public void onMarkItemFragmentInteraction() {
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        mConnectedDeviceName = data.getExtras().getString(MarkBitApplication.DEVICE_NAME);
                        mDeviceAddress = data.getExtras().getString(MarkBitApplication.DEVICE_ADDRESS);
                        Log.d(TAG, "choose: : " + mConnectedDeviceName + ": " + mDeviceAddress);

                        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mDeviceAddress);
                        mChatService.connect(device, true);
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // enabled bt
//                    Log.d(TAG, "bluetooth enabled!");
//                    bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    private void readProcess(byte[] recBytes) {
        if (recBytes[0] == (byte) 0xA5) {

            byte commandTypeIndex = recBytes[1];
            byte commandPkgIndex = recBytes[2];

            byte[] data = new byte[Constants.packageDataLen];
            System.arraycopy(recBytes, 3, data, 0, Constants.packageDataLen);

            int nextPkgIndex = commandPkgIndex + 1;
            if (nextPkgIndex < Constants.packageCntMap.get(commandTypeIndex)) {
                CommandRead command = new CommandRead(commandTypeIndex, 0);
                command.setPackageIndex(commandPkgIndex + 1);
                byte[] sendBytes = command.getCommandContent();
                mChatService.write(sendBytes);
            } else {
                // get full packages
            }
        } else {
            Log.e(TAG, "return package is invalid.");
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    protected boolean sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED && mChatService.getState() != BluetoothChatService.STATE_CONNECTING) {
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.not_connected) + " data send failed!");
            return false;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
//            mBluetoothServer.write(message);
            Log.d(TAG, "sended message: " + FileIO.bytesToHexString(message));

            int perLen = 20;
            int num = (message.length - 1) / perLen + 1;

            for (int i = 0; i < num; ++i) {
                byte[] subMes = Arrays.copyOfRange(message, i * perLen, (i+1) * perLen);
                mChatService.write(subMes);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        return true;
    }

    @Override
    public void startReadInfo(int index) {

        CommandRead commandRead = new CommandRead(37, 0);
        sendMessage(commandRead.getCommandContent());
    }
}