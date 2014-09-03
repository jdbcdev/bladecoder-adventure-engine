/*******************************************************************************
 * Copyright 2014 Rafael Garcia Moreno.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.bladecoder.engine.ui;

import org.bladecoder.engine.assets.EngineAssetManager;
import org.bladecoder.engine.model.Text;
import org.bladecoder.engine.model.TextManager;
import org.bladecoder.engine.model.World;
import org.bladecoder.engine.util.RectangleRenderer;
import org.bladecoder.engine.util.TextUtils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * TextManagerUI draws texts and dialogs on screen.
 * 
 * For now, only one subtitle is displayed in the screen.
 * 
 * @author rgarcia
 * 
 */
public class TextManagerUI extends Actor {
	private static final float RECT_MARGIN = 18f; // TODO: MARGIN DEPENDS ON
													// RESOLUTION/DPI!
	private static final float RECT_BORDER = 2f;

	private static final String FONT_STYLE = "TEXTMANAGER_FONT";

	private BitmapFont font = null;
	private float maxRectangleWidth;
	private float maxTalkWidth;

	private AtlasRegion bubblePointer;
	private float scale = 1f;
	private SceneScreen sceneScreen;
	private final Vector3 unprojectTmp = new Vector3();

	public TextManagerUI(SceneScreen sceneScreen) {
		this.sceneScreen = sceneScreen;
	}

	@Override
	public void act(float delta) {
		super.act(delta);

		Text currentSubtitle = World.getInstance().getTextManager()
				.getCurrentSubtitle();

		if (currentSubtitle == null && isVisible()) {
			setVisible(false);
		} else if (currentSubtitle != null && !isVisible()) {
			setVisible(true);
		}
	}

	@Override
	public void draw(Batch batch, float alpha) {
		Text currentSubtitle = World.getInstance().getTextManager()
				.getCurrentSubtitle();
		
		float posx = currentSubtitle.x;
		float posy = currentSubtitle.y;

		unprojectTmp.set(posx, posy, 0);
		World.getInstance().getSceneCamera()
				.scene2screen(sceneScreen.getViewport(), unprojectTmp);

		if (posx == TextManager.POS_CENTER || posx == TextManager.POS_SUBTITLE)
			posx = TextUtils.getCenterX(font, currentSubtitle.str,
					maxRectangleWidth, (int) sceneScreen.getViewport()
							.getScreenWidth());
		else
			posx = unprojectTmp.x;

		if (posy == TextManager.POS_CENTER)
			posy = TextUtils.getCenterY(font, currentSubtitle.str,
					maxRectangleWidth, (int) sceneScreen.getViewport()
							.getScreenHeight());
		else if (posy == TextManager.POS_SUBTITLE)
			posy = TextUtils.getSubtitleY(font, currentSubtitle.str,
					maxRectangleWidth, (int) sceneScreen.getViewport()
							.getScreenHeight());
		else
			posy = unprojectTmp.y;

		font.setColor(currentSubtitle.color);

		if (currentSubtitle.type == Text.Type.RECTANGLE) {

			TextBounds b = font.getWrappedBounds(currentSubtitle.str,
					maxRectangleWidth);

			RectangleRenderer.draw(batch, posx - RECT_MARGIN - RECT_BORDER,
					posy - b.height - RECT_MARGIN - RECT_BORDER, b.width
							+ (RECT_MARGIN + RECT_BORDER) * 2, b.height
							+ (RECT_MARGIN + RECT_BORDER) * 2, Color.BLACK);

			RectangleRenderer.draw(batch, posx - RECT_MARGIN, posy - b.height
					- RECT_MARGIN, b.width + RECT_MARGIN * 2, b.height
					+ RECT_MARGIN * 2, Color.WHITE);

			font.drawWrapped(batch, currentSubtitle.str, posx, posy, b.width,
					HAlignment.CENTER);
		} else if (currentSubtitle.type == Text.Type.TALK) {
			TextBounds b = font.getWrappedBounds(currentSubtitle.str,
					maxTalkWidth);

			posx = posx - b.width / 2;
			posy += b.height + bubblePointer.getRegionHeight() * scale
					+ RECT_MARGIN;

			float x = posx - RECT_MARGIN;
			float y = posy - b.height - RECT_MARGIN;
			float width = b.width + RECT_MARGIN * 2;
			float height = b.height + RECT_MARGIN * 2;

			float dx = 0, dy = 0;

			// check if the text exits the screen
			if (x < 0) {
				dx = -x + RECT_MARGIN;
			} else if (x + width > sceneScreen.getViewport().getScreenWidth()) {
				dx = -(x + width - sceneScreen.getViewport().getScreenWidth() + RECT_MARGIN);
			}

			if (y + height > sceneScreen.getViewport().getScreenHeight()) {
				dy = -(y + height - sceneScreen.getViewport().getScreenHeight());
			}

			batch.draw(bubblePointer,
					x + (width - bubblePointer.getRegionWidth()) / 2, y
							- bubblePointer.getRegionHeight() + 1 + dy,
					bubblePointer.getRegionWidth() / 2,
					bubblePointer.getRegionHeight(),
					bubblePointer.getRegionWidth(),
					bubblePointer.getRegionHeight(), scale, scale, 0);
			RectangleRenderer.draw(batch, x + dx, y + dy, width, height,
					Color.WHITE);

			font.drawWrapped(batch, currentSubtitle.str, posx + dx, posy + dy,
					b.width, HAlignment.CENTER);

		} else {
			TextBounds b = font.getWrappedBounds(currentSubtitle.str,
					maxRectangleWidth);
			font.drawWrapped(batch, currentSubtitle.str, posx, posy, b.width,
					HAlignment.CENTER);
		}
	}

	public void retrieveAssets(TextureAtlas atlas) {
		bubblePointer = atlas.findRegion("bubblepointer");

		if (font == null)
			font = EngineAssetManager.getInstance().loadFont(FONT_STYLE);

		font.setMarkupEnabled(true);
	}

	public void resize(int width, int height) {
		scale = width / (float) World.getInstance().getWidth();
		maxRectangleWidth = width / 1.7f;
		maxTalkWidth = width / 3;
		setBounds(0,0,width,height);
	}

	public void dispose() {
		EngineAssetManager.getInstance().disposeFont(font);
		font = null;
	}
}
