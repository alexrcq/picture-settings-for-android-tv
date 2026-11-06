<div align="center">
<img src="app/src/main/res/drawable-xhdpi/app_tv_logo.png"/>
<h1>Picture settings for Android TV</h1>
Simple app to customize picture on some MTK-based TVs<br><br>
<img src="/screenshots/screen1.png"/><br><br><img src="/screenshots/screen2.png"/>
</div>

## Download
[<img src="https://github.com/machiav3lli/oandbackupx/blob/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png"
    alt="Get it on GitHub"
    height="80">](https://github.com/alexrcq/picture-settings-for-android-tv/releases)

## How it works
The app grants itself the `WRITE_SECURE_SETTINGS` permission via [adblib](https://github.com/tananaev/adblib), allowing it to modify the Global Settings. A system service in the `com.android.tv.settings` package monitors this database and makes calls to hardware accordingly.

## Remote control
You can map the following actions to your TV remote buttons using the [tvQuickActions](https://play.google.com/store/apps/details?id=dev.vodik7.tvquickactions) app for Android TV, or connect and send commands via ADB through your phone or PC (for the phone, for example, you can use [MacroDroid](https://play.google.com/store/apps/details?id=com.arlosoft.macrodroid) and [ADB Shell \[Tasker Plugin\]](https://play.google.com/store/apps/details?id=com.ADBPlugin)).

> **Note**: The action names listed below were recently simplified for easier TV input. These new short action names are available starting from version **1.0.84+**. The old action names are still fully supported for backward compatibility.

### Dark mode

- `TOGGLE_DARK_MODE`
- `ENABLE_DARK_MODE`
- `DISABLE_DARK_MODE`

**Usage**:
```sh
adb shell am broadcast -a TOGGLE_DARK_MODE
```

### Screen dimming

- `TOGGLE_DIMMING`
- `ENABLE_DIMMING`
- `DISABLE_DIMMING`

**Usage**:
```sh
adb shell am broadcast -a TOGGLE_DIMMING
```

- `SET_DIMMING_POWER`

**Usage**:
```sh
adb shell am broadcast -a SET_DIMMING_POWER --ei filter_power 60
```
*Note*: The `filter_power` value ranges from 0 to 100.

### Screen control

- `TOGGLE_SCREEN_POWER`

**Usage**:
```sh
adb shell am broadcast -a TOGGLE_SCREEN_POWER
```

### Blue light filter

- `TOGGLE_BLUE_LIGHT_FILTER`

**Usage**:
```sh
adb shell am broadcast -a TOGGLE_BLUE_LIGHT_FILTER
```
