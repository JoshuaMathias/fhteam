import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import weka.core.Instance;


public class InstanceSet<Instance> extends HashSet{



	@Override
	public boolean contains(Object obj) {
		weka.core.Instance checkInst=(weka.core.Instance) obj;
		for (Object instance : this) {
			if (((weka.core.Instance) instance).stringValue(0)==checkInst.stringValue(0) && ((weka.core.Instance) instance).value(1)==checkInst.value(1))
				return true;
		}
		return false;
	}


}
