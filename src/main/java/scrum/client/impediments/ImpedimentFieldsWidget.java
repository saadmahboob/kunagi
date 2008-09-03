package scrum.client.impediments;

import scrum.client.common.AItemFieldsWidget;
import scrum.client.common.editable.AEditableTextWidget;
import scrum.client.common.editable.AEditableTextareaWidget;
import scrum.client.workspace.WorkspaceWidget;

public class ImpedimentFieldsWidget extends AItemFieldsWidget<Impediment> {

	@Override
	protected void build() {
		addField("Label", new LabelWidget());
		addField("Description", new DescriptionWidget());
	}

	class LabelWidget extends AEditableTextWidget {

		@Override
		protected String getText() {
			return item.label;
		}

		@Override
		protected void setText(String text) {
			item.label = text;
			WorkspaceWidget.impediments.table.updateSelectedRow();
		}

	}

	class DescriptionWidget extends AEditableTextareaWidget {

		@Override
		protected String getText() {
			return item.description;
		}

		@Override
		protected void setText(String text) {
			if (text == null || text.length() == 0) text = "-";
			item.description = text;
		}

	}
}