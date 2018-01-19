## 安卓手机扩音器

### 功能：

0.麦克风收音，扬声器播放，软件本身不对麦克风和扬声器的选择做逻辑控制，由系统自动完成

1.可以通过外接音响实现扩音，有线无线均可

2.戴耳机可以实现助听器效果，蓝牙耳机延迟较大，useless

3.支持回音消除和噪声抑制，通过安卓官方API实现，后续会用speex替代

### 权限及设备要求

0.麦克风权限

1.安卓版本4.1+，API 16+

### 主要实现技术

API：AudioRecord， AudioTrack， AcousticEchoCanceler， NoiseSuppressor

speex：后续将以speex代替NoiseSuppressor和AcousticEchoCanceler，已实现更多设备的支持和更好的效果