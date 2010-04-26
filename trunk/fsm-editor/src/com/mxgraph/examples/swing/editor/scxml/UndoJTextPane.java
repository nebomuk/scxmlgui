package com.mxgraph.examples.swing.editor.scxml;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class UndoJTextPane extends JTextPane {

	private static final long serialVersionUID = -5128499045192330958L;
	private AbstractDocument doc;
	private UndoManager undo;
	private UndoAction undoAction;
	private RedoAction redoAction;
	private static final String newline = "\n";

	@Override
    public void setSize(Dimension d){
        if (d.width < getParent().getSize().width)
            d.width = getParent().getSize().width;
            super.setSize(d);
    }
	@Override
    public boolean getScrollableTracksViewportWidth()
    {
        return false;
    }
	public UndoJTextPane(String initText,AbstractDocument d, UndoManager u) {
		super();
		doc=d;
		undo=u;
		if (doc==null) {
			doc=(AbstractDocument)getStyledDocument();
			//Put the initial text into the text pane.
			initDocument(initText);
		} else {
			setStyledDocument((StyledDocument) doc);
		}
		if (undo==null)
			undo=new UndoManager();

		undoAction=new UndoAction();
		redoAction=new RedoAction();

		//Start watching for undoable edits and caret changes.
		doc.addUndoableEditListener(new MyUndoableEditListener());
	}

	//This one listens for edits that can be undone.
	protected class MyUndoableEditListener
	implements UndoableEditListener {
		public void undoableEditHappened(UndoableEditEvent e) {
			//Remember the edit and update the menus.
			undo.addEdit(e.getEdit());
			undoAction.updateUndoState();
			redoAction.updateRedoState();
		}
	}

	protected void initDocument(String init) {
		if (init!=null) {
			String initString[] =init.split("\n");
			try {
				for (int i = 0; i < initString.length; i ++) {
					doc.insertString(doc.getLength(), initString[i] + newline,null);
				}
			} catch (BadLocationException ble) {
				System.err.println("Couldn't insert initial text.");
			}
		}
	}

	class UndoAction extends AbstractAction {
		public UndoAction() {
			super("Undo");
			setEnabled(undo.canUndo());
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.undo();
			} catch (CannotUndoException ex) {
				System.out.println("Unable to undo: " + ex);
				ex.printStackTrace();
			}
			updateUndoState();
			redoAction.updateRedoState();
		}

		protected void updateUndoState() {
			if (undo.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		public RedoAction() {
			super("Redo");
			setEnabled(undo.canRedo());
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undo.redo();
			} catch (CannotRedoException ex) {
				System.out.println("Unable to redo: " + ex);
				ex.printStackTrace();
			}
			updateRedoState();
			undoAction.updateUndoState();
		}

		protected void updateRedoState() {
			if (undo.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undo.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}

	public UndoManager getUndoManager() {
		return undo;
	}
	public UndoAction getUndoAction() {
		return undoAction;
	}
	public RedoAction getRedoAction() {
		return redoAction;
	}

}