package scrum.server.project;

import ilarkesto.core.base.Str;

import java.util.Set;

import scrum.client.common.LabelSupport;
import scrum.client.common.ReferenceSupport;
import scrum.server.admin.User;
import scrum.server.common.Numbered;
import scrum.server.estimation.RequirementEstimationVote;
import scrum.server.estimation.RequirementEstimationVoteDao;
import scrum.server.sprint.Task;
import scrum.server.sprint.TaskDao;

public class Requirement extends GRequirement implements Numbered, ReferenceSupport, LabelSupport {

	// --- dependencies ---

	private static TaskDao taskDao;
	private static RequirementEstimationVoteDao requirementEstimationVoteDao;

	public static void setRequirementEstimationVoteDao(RequirementEstimationVoteDao requirementEstimationVoteDao) {
		Requirement.requirementEstimationVoteDao = requirementEstimationVoteDao;
	}

	public static void setTaskDao(TaskDao taskDao) {
		Requirement.taskDao = taskDao;
	}

	// --- ---

	public String getEstimatedWorkAsString() {
		Float work = getEstimatedWork();
		if (work == null) return null;
		if (work <= 0.5f) return work.toString();
		return String.valueOf(work.intValue());
	}

	public void setEstimatedWorkAsString(String value) {
		if (Str.isBlank(value)) {
			setEstimatedWork(null);
			return;
		}
		setEstimatedWork(Float.parseFloat(value));
	}

	public boolean isInCurrentSprint() {
		if (!isSprintSet()) return false;
		return isSprint(getProject().getCurrentSprint());
	}

	public void initializeEstimationVotes() {
		for (User user : getProject().getTeamMembers()) {
			RequirementEstimationVote vote = getEstimationVote(user);
			if (vote == null) vote = requirementEstimationVoteDao.postVote(this, user);
			vote.setEstimatedWork(null);
		}
	}

	private RequirementEstimationVote getEstimationVote(User user) {
		return requirementEstimationVoteDao.getRequirementEstimationVoteByUser(this, user);
	}

	public Set<RequirementEstimationVote> getEstimationVotes() {
		return requirementEstimationVoteDao.getRequirementEstimationVotesByRequirement(this);
	}

	public void clearEstimationVotes() {
		for (RequirementEstimationVote vote : getEstimationVotes()) {
			requirementEstimationVoteDao.deleteEntity(vote);
		}
	}

	public boolean isTasksClosed() {
		for (Task task : getTasks()) {
			if (!task.isClosed()) return false;
		}
		return true;
	}

	public String getReferenceAndLabel() {
		return getReference() + " " + getLabel();
	}

	@Override
	public String getReference() {
		return scrum.client.project.Requirement.REFERENCE_PREFIX + getNumber();
	}

	@Override
	public void updateNumber() {
		if (getNumber() == 0) setNumber(getProject().generateRequirementNumber());
	}

	@Override
	public void ensureIntegrity() {
		super.ensureIntegrity();
		updateNumber();

		// delete estimation votes when closed or in sprint
		if (isClosed() || isSprintSet()) clearEstimationVotes();

		// delete when closed and older than 4 weeks
		if (isClosed() && getLastModified().getPeriodToNow().toWeeks() > 4) getDao().deleteEntity(this);

	}

	public Set<Task> getTasks() {
		return taskDao.getTasksByRequirement(this);
	}

	@Override
	public boolean isVisibleFor(User user) {
		return getProject().isVisibleFor(user);
	}

	@Override
	public String toString() {
		return getReferenceAndLabel();
	}
}
