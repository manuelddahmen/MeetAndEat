/*
 *  This file is part of Empty3.
 *
 *     Empty3 is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Empty3 is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Empty3.  If not, see <https://www.gnu.org/licenses/>. 2
 */

package one.empty3.library.core.nurbs;

import one.empty3.library.Point3D;
import one.empty3.library.StructureMatrix;

/*__
 * Created by manue on 23-11-19.
 */
public class ExtrusionCurveCurve extends ParametricSurface {
    private StructureMatrix<ParametricCurve> base = new StructureMatrix<>(0, ParametricCurve.class);
    private StructureMatrix<ParametricCurve> path = new StructureMatrix<>(0, ParametricCurve.class);

    @Override
    public void declareProperties() {
        super.declareProperties();
        getDeclaredDataStructure().put("base/Surface à extruder", base);
        getDeclaredDataStructure().put("path/Chemin d'extrusion", path);
    }

    public Point3D calculerPoint3D(double u, double v) {
        return base.getElem().calculerPoint3D(u).plus(path.getElem().calculerPoint3D(v));

    }


}