(
SuperDirt.postBadValues = false;


s.options.device_("BlackHole 16ch");
s.options.numBuffers = 1024 * 16;
s.options.memSize = 8192 * 16;
s.options.maxNodes = 1024 * 64;
s.options.numOutputBusChannels = 8;
s.options.numInputBusChannels = 0;

s.waitForBoot {
	~dirt = SuperDirt(2, s);


	s.sync;
	~dirt.start(57120, [0]);

	// ~dirt.loadSoundFiles("~/studio/tidal-samples/breaks/*");

	MIDIClient.init;
	">>>> loading MIDI devices".postln;

	MIDIClient.sources.do({ arg item, i;
		"🤔 ".catArgs(item.device).catArgs(" ").catArgs(item.name).postln;

		if (item.name == "Bus 1", {
			~harmorOut = MIDIOut.newByName("IAC Driver", "Bus 1");
			~harmorOut.latency = 0;
			~dirt.soundLibrary.addMIDI(\harmor, ~harmorOut);
			"😍 added harmor".postln;
		});

		if (item.device == "MIDI 1U", {
			~modOut = MIDIOut.newByName("MIDI 1U", "MIDI 1U");
			~modOut.latency = 0;
			~dirt.soundLibrary.addMIDI(\mod, ~modOut);
			"😍 added modular".postln;
		});

		if (item.device == "Elektron Analog Rytm MKII", {
			~rytmOut = MIDIOut.newByName("Elektron Analog Rytm MKII", "Elektron Analog Rytm MKII");
			~rytmOut.latency = 0;
			~dirt.soundLibrary.addMIDI(\rytm, ~rytmOut);
			"😍 added RYTM".postln;
		});

		if (item.device == "Elektron Syntakt", {
			~synOut = MIDIOut.newByName("Elektron Syntakt", "Elektron Syntakt");
			~synOut.latency = 0;
			~dirt.soundLibrary.addMIDI(\syntakt, ~synOut);
			"😍 added Syntakt".postln;
		});

		if (item.device == "Midi Fighter Twister", {
			"attempting to connect to MIDI input to Midi Fighter Twister".postln;
			"going for device at index".catArgs(" ").catArgs(1).catArgs(", ").catArgs("whatever it is:").postln;
			MIDIClient.sources.at(i).device.catArgs(" ").catArgs(MIDIClient.sources.at(i).name).postln;

		    ~osc = NetAddr.new("127.0.0.1", 6010);

			MIDIIn.connect(inport: 0, device: i);

			MIDIFunc.cc({ |val, num, chan, src|
				// chan.postln;
				if (chan == 0, {
  				"debug: ".catArgs(chan).catArgs(" ").catArgs(num).catArgs(" ").catArgs(val).postln;
					~osc.sendMsg("/ctrl", num.asString, val/127);
				});
			});

			"MIDI input attached".postln
		});


	});

	"<<<< done loading MIDI devices".postln;



	// var addr = NetAddr.new("10.0.0.244", 5150);
	"setting up OSC forwarding to 5150".postln;
	~addr = NetAddr.new("127.0.0.1", 5150);
	OSCFunc({ |msg, time, tidalAddr|
		var latency = time - Main.elapsedTime;
		msg = msg ++ ["time", time, "latency", latency];
		// msg.postln;
		~addr.sendBundle(latency, msg);
	}, '/dirt/play').fix;

};
s.latency = 0;
);

































(
// var addr = NetAddr.new("10.0.0.244", 5150);
var addr = NetAddr.new("127.0.0.1", 5150);
OSCFunc({ |msg, time, tidalAddr|
    var latency = time - Main.elapsedTime;
    msg = msg ++ ["time", time, "latency", latency];
	// msg.postln;
    addr.sendBundle(latency, msg);
}, '/dirt/play').fix;
)







// Evaluate the block below to start the mapping MIDI -> OSC.
(
var on, off, cc;
var osc;

osc = NetAddr.new("127.0.0.1", 6010);

MIDIClient.init;
//MIDIIn.connectAll;

MIDIIn.connect(inport: 0, device: 2);

// on = MIDIFunc.noteOn({ |val, num, chan, src|
// 	osc.sendMsg("/ctrl", num.asString, val/127);
// });

// off = MIDIFunc.noteOff({ |val, num, chan, src|
// 	osc.sendMsg("/ctrl", num.asString, 0);
// });

cc = MIDIFunc.cc({ |val, num, chan, src|
	osc.sendMsg("/ctrl", num.asString, val/127);
	// num.postln;
	// val.postln;
});

if (~stopMidiToOsc != nil, {
	~stopMidiToOsc.value;
});

~stopMidiToOsc = {
	on.free;
	off.free;
	cc.free;
};
)

// Evaluate the line below to stop it.
~stopMidiToOsc.value;


SuperDirt.start
