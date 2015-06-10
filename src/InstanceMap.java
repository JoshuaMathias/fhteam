import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import weka.core.Instance;


public class InstanceMap<Instance,Integer> extends HashMap{



	@Override
	public boolean containsKey(Object obj) {
		weka.core.Instance checkInst=(weka.core.Instance) obj;
		for (Object instance : this.keySet()) {
			if (((weka.core.Instance) instance).stringValue(0)==checkInst.stringValue(0) && ((weka.core.Instance) instance).value(1)==checkInst.value(1))
				return true;
		}
		return false;
	}


}
