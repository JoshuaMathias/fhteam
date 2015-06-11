import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import weka.core.Instance;

public class InstanceMap<Instance, Integer> extends HashMap {

	@Override
	public boolean containsKey(Object obj) {
		weka.core.Instance checkInst = (weka.core.Instance) obj;
		for (Object instance : this.keySet()) {
			if (((weka.core.Instance) instance).stringValue(0).equals(
					checkInst.stringValue(0))
					&& ((weka.core.Instance) instance).value(1) == checkInst
							.value(1))
				return true;
		}
		return false;
	}

	@Override
	public Integer get(Object obj) {
		weka.core.Instance checkInst = (weka.core.Instance) obj;
		Set<Map.Entry<Instance, Integer>> entries = this.entrySet();
		for (Map.Entry<Instance, Integer> instanceEntry : entries) {
			Instance currentInstance = instanceEntry.getKey();
			if (((weka.core.Instance) currentInstance).stringValue(0).equals(
					checkInst.stringValue(0))
					&& ((weka.core.Instance) currentInstance).value(1) == checkInst
							.value(1)) {
				return instanceEntry.getValue();
			}
			// else if (((weka.core.Instance)
			// currentInstance).stringValue(0).equals(checkInst
			// .stringValue(0))) {
			// System.out.println(((weka.core.Instance) currentInstance)
			// .stringValue(0)+":"+((weka.core.Instance) currentInstance)
			// .value(0) + " = " +
			// checkInst.stringValue(0)+":"+((weka.core.Instance)
			// currentInstance)
			// .value(1));
			// }
		}
		return null;
	}

	@Override
	public Integer put(Object instance, Object i) {
		Integer intValue = (Integer) i;
		Instance checkInst = (Instance) instance;
		Integer foundValue = this.get(checkInst);
		if (foundValue != null) {
			Set<Map.Entry<Instance, Integer>> entries = this.entrySet();
			for (Map.Entry<Instance, Integer> instanceEntry : entries) {
				Instance currentInstance = instanceEntry.getKey();
				if (((weka.core.Instance) currentInstance)
						.stringValue(0)
						.equals(((weka.core.Instance) checkInst).stringValue(0))
						&& ((weka.core.Instance) currentInstance).value(1) == ((weka.core.Instance) checkInst)
								.value(1))
					return instanceEntry.setValue(intValue);
			}
		} else {
			super.put(instance, i);
		}
		return foundValue;
	}

}
