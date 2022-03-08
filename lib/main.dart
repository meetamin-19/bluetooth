import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({Key? key, required this.title}) : super(key: key);

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const channel = MethodChannel('untitled3/simple');

  List deviceList = [];

  // Get battery level.
  String _batteryLevel = 'Unknown battery level.';

  Future<void> _getBatteryLevel() async {
    String batteryLevel;
    try {
      final int result = await channel.invokeMethod('getBatteryLevel');
      batteryLevel = 'Battery level at $result % .';
    } on PlatformException catch (e) {
      batteryLevel = "Failed to get battery level: '${e.message}'.";
    }

    setState(() {
      _batteryLevel = batteryLevel;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Material(
      child: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            ElevatedButton(
              child: const Text('Get Battery Level'),
              onPressed: _getBatteryLevel,
            ),
            Text(_batteryLevel),
            ElevatedButton(
                onPressed: _turnOnBluetooth,
                child: const Text("turn on bluetooth")),
            ElevatedButton(
              onPressed: _turnOffBluetooth,
              child: const Text("Turn off Bluetooth"),
            ),
            ElevatedButton(
                onPressed: _getBluetoothdevices,
                child: const Text("Get Bluetooth devices"))
          ],
        ),
      ),
    );
  }

  Future<void> _turnOnBluetooth() async {
    String bluetooth;
    try {
      final String result = await channel.invokeMethod('bluetooth');
      print(result);
    } on PlatformException catch (e) {
      bluetooth = "Failed to turn on Bluetooth : '${e.message}";
    }
  }

  Future<void> _turnOffBluetooth() async {
    try {
      await channel.invokeMethod('off');
    } on PlatformException catch (e) {
      print("Unable to turn off Bluetooth : '${e.message}");
    }
  }

  Future<void> _getBluetoothdevices() async {
    List bludevices = <dynamic>[];
    try {
      bludevices = await channel.invokeMethod('scan');
      print("cool blue");
    } on PlatformException catch (e) {
      print("failed to get bluetooth devices : ${e.message}");
    }

    setState(() {
      if (bludevices.isNotEmpty) {
        deviceList = bludevices;
      }
    });
  }
}
