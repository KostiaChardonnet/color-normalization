package plugins.normalization;

import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarEnum;
import plugins.adufour.ezplug.EzVarFloat;
import plugins.adufour.ezplug.EzVarSequence;
import algorithms.normalization.*;
import icy.sequence.Sequence;
import icy.sequence.SequenceUtil;

public class Normalisation extends EzPlug {

	private EzVarSequence target = new EzVarSequence("target");
	private EzVarSequence source = new EzVarSequence("source");
	//private EzVarFloat minIntensity = new EzVarFloat("Minimum intensity : ");
	private EzVarEnum<Choice> choice = new EzVarEnum<Choice>("choice", Choice.values(), Choice.Reinhard);

	enum Choice {
		Macenko,
		Reinhard
	}

	@Override
	public void run() {

		if(target.getValue().getSizeC() == 4)
			SequenceUtil.removeChannel(target.getValue(), 3);
		if(source.getValue().getSizeC() == 4)
			SequenceUtil.removeChannel(source.getValue(), 3);

		if(this.choice.getValue().equals(Choice.Macenko))
		{	
			Sequence newSeq = new NormMacenko(target.getValue().getImage(0, 0), source.getValue().getImage(0, 0)).getSeq(); //, this.minIntensity.getValue()).getSeq();
			newSeq.setName(source.getValue().getName() + "- Macenko");
			super.addSequence(newSeq);

		}
		else if(this.choice.getValue().equals(Choice.Reinhard))
		{
			Sequence newSeq = new NormReinhard(target.getValue().getImage(0,0), source.getValue().getImage(0,0)).getSeq();
			newSeq.setName(source.getValue().getName() + "- Reinhard");
			super.addSequence(newSeq);
		}

	}


	@Override
	public void clean() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void execute() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initialize() {

		super.addEzComponent(choice);
//		super.addEzComponent(minIntensity);
		super.addEzComponent(target);
		super.addEzComponent(source);

	}

}
