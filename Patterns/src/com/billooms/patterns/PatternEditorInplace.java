package com.billooms.patterns;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;

/**
 * InplaceEditor for editing the cutter pattern
 *
 * @author Bill Ooms. Copyright 2015 Studio of Bill Ooms. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class PatternEditorInplace extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {

  private static ExplorerManager em = null;   // all instances share one ExplorerManager

  /**
   * This method is called by the property sheet to pass the environment to the
   * property editor.
   *
   * @param env
   */
  @Override
  public void attachEnv(PropertyEnv env) {
    env.registerInplaceEditorFactory(this);
  }

  /**
   * Fetch or create an inplace editor instance.
   *
   * @return An inplace editor instance
   */
  @Override
  public InplaceEditor getInplaceEditor() {
    if (em == null) {	// only do this once
      em = ((ExplorerManager.Provider) WindowManager.getDefault().findTopComponent("DataNavigatorTopComponent")).getExplorerManager();
    }
    return new PatternInplaceEditor();
  }

  private static class PatternInplaceEditor implements InplaceEditor {

    private PropertyEditor editor;
    private PropertyModel model;
    private final JComboBox<String> patternCombo;
    private final Patterns patternMgr;

    public PatternInplaceEditor() {
      this.patternCombo = new JComboBox<>();

      Node rootNode = em.getRootContext();    // it won't be null because we're clicking on it
      patternMgr = rootNode.getLookup().lookup(Patterns.class);
      patternMgr.getAllDisplayNames().stream().forEach((s) -> {
        patternCombo.addItem(s);
      });
    }

    @Override
    public void connect(PropertyEditor pe, PropertyEnv env) {
      this.editor = pe;
      this.reset();
    }

    @Override
    public JComponent getComponent() {
      return this.patternCombo;
    }

    @Override
    public void clear() {
      this.editor = null;
      this.model = null;
    }

    @Override
    public Object getValue() {
      return patternMgr.get(patternCombo.getSelectedIndex());
    }

    @Override
    public void setValue(Object o) {
      patternCombo.setSelectedIndex(patternMgr.indexOf(((Pattern) o).getName()));
    }

    @Override
    public boolean supportsTextEntry() {
      return false;
    }

    @Override
    public void reset() {
      Pattern p = (Pattern) this.editor.getValue();
      this.setValue(p);
    }

    @Override
    public void addActionListener(ActionListener al) {
    }

    @Override
    public void removeActionListener(ActionListener al) {
    }

    @Override
    public KeyStroke[] getKeyStrokes() {
      return new KeyStroke[0];
    }

    @Override
    public PropertyEditor getPropertyEditor() {
      return this.editor;
    }

    @Override
    public PropertyModel getPropertyModel() {
      return this.model;
    }

    @Override
    public void setPropertyModel(PropertyModel pm) {
      this.model = pm;
    }

    @Override
    public boolean isKnownComponent(Component c) {
      return c == this.patternCombo || this.patternCombo.isAncestorOf(c);
    }

  }

}
