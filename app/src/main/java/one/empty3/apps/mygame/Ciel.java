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

/*
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

/*__
Global license : 

 CC Attribution

 author Manuel Dahmen _manuel.dahmen@gmx.com_

 ***/


package one.empty3.apps.mygame;

import javaAnd.awt.Color;
import javaAnd.awt.image.BufferedImage;
import javaAnd.awt.image.imageio.ImageIO;
import one.empty3.library.*;

import java.io.File;
import java.net.URL;

/*__
 *
 * @author Manuel Dahmen _manuel.dahmen@gmx.com_
 */
public class Ciel {
    private final Sphere bleu;

    public Ciel() {
        bleu = new Sphere(new Point3D(0.5, 0.5, 0.0), 2);

        try {
            URL resource = getClass().getResource("res/img/planets/mars-surface.jpg");
            System.out.println(resource.toExternalForm());
            BufferedImage read = ImageIO.read(new File(resource.getFile()));
            ECBufferedImage ecBufferedImage = new ECBufferedImage(read.bitmap);
            bleu.texture(new TextureImg(ecBufferedImage));

        } catch (Exception  ex) {
            bleu.texture(new ColorTexture(Color.BLUE));
        }
    }

    public Sphere getBleu() {
        return bleu;
    }


}
