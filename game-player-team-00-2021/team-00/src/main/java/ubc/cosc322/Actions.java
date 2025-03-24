package ubc.cosc322;

import java.util.List;

public class Actions {
	public List<Integer> queenCurrent;
	public List<Integer> queenTarget;
	public List<Integer> arrowTarget;

	public Actions(List<Integer> queenCurrent, List<Integer> queenTarget, List<Integer> arrowTarget) {
		this.queenCurrent = queenCurrent;
		this.queenTarget = queenTarget;
		this.arrowTarget = arrowTarget;
	}
}
