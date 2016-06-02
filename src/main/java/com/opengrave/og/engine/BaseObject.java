/*
 * Copyright 2016 Nathan Howard
 * 
 * This file is part of OpenGrave
 * 
 * OpenGrave is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OpenGrave is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with OpenGrave. If not, see <http://www.gnu.org/licenses/>.
 */
package com.opengrave.og.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.MenuInfo;
import com.opengrave.common.OGOutputStream;
import com.opengrave.common.PopupMenuOption;
import com.opengrave.common.pathing.Path;
import com.opengrave.common.pathing.Point;
import com.opengrave.common.world.CommonLocation;
import com.opengrave.common.world.CommonObject;
import com.opengrave.common.world.CommonObject.Type;
import com.opengrave.common.world.MaterialList;
import com.opengrave.og.Util;
import com.opengrave.og.base.Pickable;
import com.opengrave.og.base.Renderable3D;
import com.opengrave.og.resources.RenderStyle;
import com.opengrave.og.util.Matrix4f;
import com.opengrave.og.util.Vector3f;

public abstract class BaseObject extends Node implements Pickable {

	private String identifier = "unknown";
	public Renderable3D renderable;
	protected MaterialList matList;
	private String modelLabel;
	public Location location;
	public RenderStyle style;
	protected CommonObject cobj;
	private Path path;
	protected float timeSinceTick = 0f;
	protected double lastTick, nextTick;

	protected Surface s;
	protected Point lookPoint;
	protected BaseObject lookObj;
	protected boolean visible = true;
	public boolean drawOutline = true;

	public void moveBasedOnPath(float delta) {
		if (path == null) {
			this.stopAnimation("walk");
			return;
		}
		timeSinceTick += delta;
		double time = timeSinceTick;
		if (time > 250f) { // 250ms, once tick len
			// this.stopAnimation("walk"); // We've gone as far as we can this
			// tick, server may be running too slow. Don't walk-anim on the
			// spot.
			time = 250f;
		} else {
			this.startAnimation("walk", 0.3f / 1000f, false);
		}
		time = time / 250.0;
		double dist = lastTick + ((nextTick - lastTick) * time);
		CommonLocation cl = path.getLocation(dist);
		location.setFullX(cl.getFullXAsFloat());
		location.setFullY(cl.getFullYAsFloat());
		location.setLayer(cl.getLayer());
	}

	public void tick(double lastTick, double nextTick) {
		timeSinceTick = 0f;
		this.lastTick = lastTick;
		this.nextTick = nextTick;
	}

	public void setSurface(Surface s) {
		this.s = s;
	}

	public BaseObject(CommonObject cobj) {
		this.cobj = cobj;
		identifier = cobj.getIdentifier();
		matList = cobj.getMaterialList();
		modelLabel = cobj.getModelLabel();
		location = new Location(cobj.getLocation());
		renderableLabelChanged(modelLabel);
	}

	public abstract String getType();

	public void save(OGOutputStream stream) {

		try {
			stream.writeString(getType());
			stream.writeString(modelLabel);
			stream.writeLocation(location);
			stream.writeMaterialList(matList);
		} catch (IOException e) {
			new DebugExceptionHandler(e);

		}

	}

	public String getModelLabel() {
		return modelLabel;
	}

	public float getX() {
		return location.getFullXAsFloat();
	}

	public float getY() {
		return location.getFullYAsFloat();
	}

	public float getZ() {
		return location.getZ();
	}

	public Vector3f getAngles() {
		return location.getRotate();
	}

	public void setMaterialList(MaterialList matList) {
		this.matList = matList;
	}

	public String getRenderableLabel() {
		return modelLabel;
	}

	public void setRenderableLabel(String s) {
		renderableLabelChanged(s);
		modelLabel = s;
	}

	public BaseObject setLocation(CommonLocation l) {
		location = new Location(l);
		return this;
	}

	public Location getLocation() {
		return location;
	}

	public BaseObject setRenderable(String s) {
		cobj.setModelLabel(s);
		setRenderableLabel(s);
		// renderable = Resources.getStaticModel(s).getRenderable();
		return this;
	}

	public abstract void renderableLabelChanged(String s);

	public void setX(float number) {
		location.setFullX(number);
	}

	public void setY(float number) {
		location.setFullY(number);

	}

	public void setZ(float number) {
		location.setZ(number);

	}

	public void setAngle(Vector3f vector3f) {
		// location.getRotate().z = number;
		// Vector3f r = location.getRotate();
		location.setRotate(vector3f.x, vector3f.y, vector3f.z);
	}

	public void setRenderStyle(RenderStyle style) {
		this.style = style;
	}

	public abstract void startAnimation(String name, float speed, boolean once);

	public abstract void stopAnimation(String name);

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String string) {
		identifier = string;
	}

	public RenderView getContext() {
		return context;
	}

	@Override
	public Matrix4f getMatrix() {
		return Util.createMatrixFor(location, null, null, null);
	}

	public BaseObject clone() {
		return createObject(cobj);
	}

	public abstract BoundingBox getBoundingBox();

	public String getRenderableFile() {
		int pos = modelLabel.indexOf(":");
		return modelLabel.substring(0, pos);
	}

	public String getRenderableSection(int i) {
		String[] split = modelLabel.split(":");
		if (i + 1 >= split.length) {
			return "";
		}
		return split[i + 1];
	}

	public MaterialList getMaterialList() {
		return matList;
	}

	public int getRenderableSectionCount() {
		String[] split = modelLabel.split(":");
		return split.length - 1;
	}

	public void setRenderableSection(int number, String text) {
		String[] split = modelLabel.split(":", -1); // -1 keeps empty trailing
													// elements, which is
													// correct here
		String newS = split[0];
		int last = split.length;
		if (number + 1 >= last) {
			last = number + 2;
		} // These numbers looks so wrong, but it works so meh
		for (int i = 1; i < last; i++) {
			if (number == i - 1) {
				newS += ":" + text;
			} else {
				if (i < last) {
					newS += ":" + split[i];
				} else {
					newS += ":";
				}
			}
		}
		setRenderable(newS);
	}

	public void setRenderableFile(String fileName) {
		int i = modelLabel.indexOf(":");
		if (i == -1) {
			setRenderable(fileName);
			return;
		}
		String end = modelLabel.substring(modelLabel.indexOf(":"));
		setRenderable(fileName + end);
	}

	public static BaseObject createObject(CommonObject cobj) {
		if (cobj == null) {
			return null;
		}
		Type cT = cobj.getType();
		if (cT == Type.Static) {
			return new StaticObject(cobj);
		} else if (cT == Type.Anim) {
			AnimatedObject a = new AnimatedObject(cobj);
			// TODO Smarter setup of gaits and walk systems
			// if(cobj.getModelLabel().startsWith("mod/craig.dae")){
			// a.setWalk(new BipedWalk(a,
			// a.getSkeleton().getBone("UpperLeg.left"),
			// a.getSkeleton().getBone("UpperLeg.right")));
			// }
			return a;
		} else if (cT == Type.Particle) {
			return new ParticleObject(cobj);
		}
		return null;
	}

	public CommonObject getCommonObject() {
		return cobj;
	}

	public UUID getUUID() {
		return cobj.getUUID();
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public void lookAt(Point point) {
		lookObj = null;
		lookPoint = point;
	}

	public void lookAt() {
		Point point = lookPoint;
		if (lookPoint == null) {
			if (lookObj == null) {
				return;
			}
			point = new Point(lookObj.getLocation());
		}
		Point here = new Point(getLocation());
		if (point.getDistance(here) < 0.1) {
			return;
		}
		double angle = Math.toDegrees(here.getAngle(point)) - 270;
		getLocation().setAngleZ((float) angle);
	}

	public void setVisible(boolean checked) {
		visible = checked;
	}

	public void moveTo(CommonLocation l) {
		setLocation(l);
	}

	public void lookDir(Point point) {
		lookPoint = null;
		lookObj = null;
		Point here = new Point(getLocation());
		Point inc = new Point(here);
		inc.add(point);
		if (here.getDistance(inc) < 0.1) {
			return;
		}
		double angle = Math.toDegrees(here.getAngle(inc)) - 90;
		getLocation().setAngleZ((float) angle);
	}

	public ArrayList<PopupMenuOption> createMenuList(String context, int page) {
		// TODO Populate popup menu from actions available on server. Best to
		// send with the CommonObject over net
		ArrayList<PopupMenuOption> list = new ArrayList<PopupMenuOption>();
		list.add(new PopupMenuOption("cancel", "tex/guicross.png", "Cancel"));
		if (context.equals("none")) {
			// No context
			list.add(new PopupMenuOption("menu:interact:0", "tex/guiinteract.png", "Interact"));
			list.add(new PopupMenuOption("menu:combat:0", "tex/guicombat.png", "Combat"));
			list.add(new PopupMenuOption("menu:spell:0", "tex/guispell.png", "Spell"));
		} else if (context.equals("interact")) {
			list.add(new PopupMenuOption("talk", "tex/guitalk.png", "Talk"));
			list.add(new PopupMenuOption("pickpocket", "tex/guipickpocket.png", "Pickpocket"));
			list.add(new PopupMenuOption("trade", "tex/guitrade.png", "Trade"));
		} else if (context.equals("combat")) {
			list.add(new PopupMenuOption("kill", "tex/guikill.png", "Kill"));
			list.add(new PopupMenuOption("subdue", "tex/guisubdue.png", "Subdue"));
			list.add(new PopupMenuOption("kidnap", "tex/guikidnap.png", "Kidnap"));
		} else if (context.equals("spell")) {
			list.add(new PopupMenuOption("influence", "tex/guiinfluence.png", "Influence"));
			list.add(new PopupMenuOption("confuse", "tex/guiconfuse.png", "Confuse"));
			list.add(new PopupMenuOption("heal", "tex/guiheal.png", "Heal"));
		}

		return list;
	}

	public RenderStyle getRenderStyle() {
		return style;
	}

	public MenuInfo getMenuInfo() {
		return cobj.getMenuInfo();
	}

}
