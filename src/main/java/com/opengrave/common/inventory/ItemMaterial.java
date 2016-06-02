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
package com.opengrave.common.inventory;

public class ItemMaterial {
	String name;
	float r, g, b, a;
	float edge, lightarmour, heavyarmour, mass, springiness, tautness;

	public ItemMaterial(String name, float edge, float lightarmour, float heavyarmour, float mass, float springiness, float tautness, float r, float g,
			float b, float a) {
		this.name = name;
		this.edge = edge;
		this.lightarmour = lightarmour;
		this.heavyarmour = heavyarmour;
		this.mass = mass;
		this.springiness = springiness;
		this.tautness = tautness;
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public String getName() {
		return name;
	}

	public float getR() {
		return r;
	}

	public float getG() {
		return g;
	}

	public float getB() {
		return b;
	}

	public float getA() {
		return a;
	}

	public float getEdge() {
		return edge;
	}

	public float getLightarmour() {
		return lightarmour;
	}

	public float getHeavyarmour() {
		return heavyarmour;
	}

	public float getMass() {
		return mass;
	}

	public float getSpringiness() {
		return springiness;
	}

	public float getTautness() {
		return tautness;
	}

}
