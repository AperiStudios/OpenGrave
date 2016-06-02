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
package com.opengrave.common.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.opengrave.common.DebugExceptionHandler;
import com.opengrave.common.OGInputStream;
import com.opengrave.common.ModSession;
import com.opengrave.common.pathing.Point;

public class CommonWorldLoader implements Runnable {

	private CommonWorld world;
	private ModSession sess;

	public CommonWorldLoader(CommonWorld world, ModSession sess) {
		this.world = world;
		this.sess = sess;
	}

	@Override
	public void run() {
		File f = new File(world.getDirectory(), "collision.data");
		System.out.println("Loading world file " + f.getAbsolutePath());
		if (f.isFile()) {
			try (OGInputStream in = new OGInputStream(new FileInputStream(f))) {
				int numberOfAreas = in.readInt();
				for (int i = 0; i < numberOfAreas; i++) {
					int areaX = in.readInt();
					int areaY = in.readInt();
					CommonArea ca = new CommonArea();
					int numberOfObjects = in.readInt();
					for (int o = 0; o < numberOfObjects; o++) {
						CommonObject obj = new CommonObject(in);
						ca.addObject(obj);
						sess.getObjectStorage().addObject(obj);
					}
					world.addArea(areaX, areaY, ca);
				}
				int numberOfPolygons = in.readInt();
				world.navMesh.delayEdges = true; // Don't rebuild edges every single poly added
				for (int i = 0; i < numberOfPolygons; i++) {
					int verts = in.readInt();
					ArrayList<Point> points = new ArrayList<Point>();
					for (int j = 0; j < verts; j++) {
						double x = in.readDouble();
						double y = in.readDouble();
						int z = in.readInt();
						Point p = new Point(x, y, z);
						world.navMesh.addPoint(p);
						points.add(p);
					}
					world.navMesh.addPoly(points);
				}
				world.navMesh.delayEdges = false;
				world.navMesh.fixEdges();
			} catch (FileNotFoundException e) {
				new DebugExceptionHandler(e);
			} catch (IOException e) {
				new DebugExceptionHandler(e);
			}
		}

		world.setLoaded();
	}

}
