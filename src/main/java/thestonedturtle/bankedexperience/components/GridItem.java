/*
 * Copyright (c) 2019, TheStonedTurtle <https://github.com/TheStonedTurtle>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package thestonedturtle.bankedexperience.components;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import lombok.Setter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import thestonedturtle.bankedexperience.BankedCalculator;
import thestonedturtle.bankedexperience.data.Activity;
import thestonedturtle.bankedexperience.data.BankedItem;
import thestonedturtle.bankedexperience.data.modifiers.Modifier;

@Getter
public class GridItem extends JLabel
{
	private final static String IGNORE = "Ignore Item";
	private final static String IGNORE_ALL = "Ignore All Items";
	private final static String INCLUDE = "Include Item";
	private final static String INCLUDE_ALL = "Include All Items";

	private static final Color UNSELECTED_BACKGROUND = ColorScheme.DARKER_GRAY_COLOR;
	private static final Color UNSELECTED_HOVER_BACKGROUND = ColorScheme.DARKER_GRAY_HOVER_COLOR;

	public static final Color SELECTED_BACKGROUND = new Color(0, 70, 0);
	private static final Color SELECTED_HOVER_BACKGROUND =  new Color(0, 100, 0);

	public static final Color IGNORED_BACKGROUND = new Color(90, 0, 0);
	private static final Color IGNORED_HOVER_BACKGROUND = new Color(120, 0, 0);

	private static final Color RNG_BACKGROUND = new Color(140, 90, 0);
	private static final Color RNG_HOVER_BACKGROUND = new Color(186, 120, 0);

	@Setter
	private SelectionListener selectionListener;

	private final BankedItem bankedItem;
	private int amount;

	private boolean selected = false;
	private boolean ignored;
	private boolean rng;

	private final JMenuItem IGNORE_OPTION = new JMenuItem(IGNORE);
	private final JMenuItem IGNORE_ALL_OPTION = new JMenuItem(IGNORE_ALL);
	private final JMenuItem INCLUDE_ALL_OPTION = new JMenuItem(INCLUDE_ALL);

	GridItem(final BankedItem item, final AsyncBufferedImage icon, final int amount,
			 final Collection<Modifier> modifiers, final boolean ignore, Consumer<Boolean> bulkIgnoreCallback)
	{
		super("");

		this.setIgnore(ignore);
		this.bankedItem = item;

		this.setOpaque(true);
		this.setBorder(BorderFactory.createEmptyBorder(5, 0, 2, 0));

		this.setVerticalAlignment(SwingConstants.CENTER);
		this.setHorizontalAlignment(SwingConstants.CENTER);

		updateIcon(icon, amount);
		updateToolTip(modifiers);

		this.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent mouseEvent)
			{
				if (mouseEvent.getButton() == MouseEvent.BUTTON1)
				{

					if (selectionListener != null && !selectionListener.selected(item))
					{
						return;
					}

					select();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				final GridItem item = (GridItem) e.getSource();
				item.setBackground(getHoverBackgroundColor());
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				final GridItem item = (GridItem) e.getSource();
				item.setBackground(getBackgroundColor());
			}
		});

		IGNORE_OPTION.addActionListener(e ->
		{
			ignored = !ignored;
			if (selectionListener != null && !selectionListener.ignored(item))
			{
				ignored = !ignored;
				return;
			}

			setIgnore(ignored);
		});

		IGNORE_ALL_OPTION.addActionListener(e -> {
			bulkIgnoreCallback.accept(true);
		});

		INCLUDE_ALL_OPTION.addActionListener(e -> {
			bulkIgnoreCallback.accept(false);
		});

		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.setBorder(new EmptyBorder(5, 5, 5, 5));
		popupMenu.add(IGNORE_OPTION);
		popupMenu.add(INCLUDE_ALL_OPTION);
		popupMenu.add(IGNORE_ALL_OPTION);

		this.setComponentPopupMenu(popupMenu);
	}

	private Color getBackgroundColor()
	{
		return ignored ? IGNORED_BACKGROUND : (rng ? RNG_BACKGROUND : (selected ? SELECTED_BACKGROUND : UNSELECTED_BACKGROUND));
	}

	private Color getHoverBackgroundColor()
	{
		return ignored ? IGNORED_HOVER_BACKGROUND : (rng ? RNG_HOVER_BACKGROUND : (selected ? SELECTED_HOVER_BACKGROUND : UNSELECTED_HOVER_BACKGROUND));
	}

	void select()
	{
		selected = true;
		setBackground(getBackgroundColor());
	}

	void unselect()
	{
		selected = false;
		setBackground(getBackgroundColor());
	}

	public void updateIcon(final AsyncBufferedImage icon, final int amount)
	{
		icon.addTo(this);
		this.amount = amount;
	}

	public void updateToolTip(final Collection<Modifier> modifiers)
	{
		this.setToolTipText(buildToolTip(modifiers));
		final Activity selectedActivity = bankedItem.getItem().getSelectedActivity();
		if (selectedActivity != null)
		{
			this.rng = selectedActivity.isRngActivity();
			this.setBackground(getBackgroundColor());
		}
	}

	private String buildToolTip(final Collection<Modifier> modifiers)
	{
		String tip = "<html>" + bankedItem.getItem().getItemInfo().getName();

		final Activity a = bankedItem.getItem().getSelectedActivity();
		if (a != null)
		{
			final double xp = a.getXpRate(modifiers);
			tip += "<br/>Activity: " +  a.getName();
			tip += "<br/>Xp/Action: " + BankedCalculator.XP_FORMAT_COMMA.format(xp);
			tip += "<br/>Total Xp: " + BankedCalculator.XP_FORMAT_COMMA.format(xp * amount);
		}
		else
		{
			tip += "<br/>Unusable at current level";
		}

		return tip + "</html>";
	}

	public void setIgnore(Boolean ignored) {
		this.ignored = ignored;
		IGNORE_OPTION.setText(ignored ? INCLUDE : IGNORE);
		this.setBackground(this.getBackgroundColor());
	}
}
