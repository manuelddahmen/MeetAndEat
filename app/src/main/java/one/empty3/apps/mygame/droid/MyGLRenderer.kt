package one.empty3.apps.mygame.droid

import android.graphics.Color
import android.opengl.GLES11
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLU
import one.empty3.apps.mygame.*
import one.empty3.library.*
import one.empty3.library.core.nurbs.CourbeParametriquePolynomiale
import one.empty3.library.core.nurbs.ParametricCurve
import one.empty3.library.core.nurbs.ParametricSurface
import one.empty3.library.core.tribase.TRIObjetGenerateur
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.function.Consumer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10



class MyGLRenderer() : GLSurfaceView.Renderer {
    private lateinit var plotter3D: Plotter3D
    private lateinit var toggleMenu: ToggleMenu
    private lateinit var circuit: Circuit
    private lateinit var glu: GLU
    private lateinit var timer: Timer
    private lateinit var bonus: Bonus
    private lateinit var vaisseau: Vaisseau
    private val INCR_AA: Double = 0.01
    private var terrain: Terrain = SolPlan()
    private lateinit var mover: PositionUpdate


    init {
        this.terrain = SolPlan()
        this.mover = PositionUpdateImpl(terrain, Player("Man",
            Color.valueOf(0f,0f,0f), 1))
        this.vaisseau = Vaisseau(mover)
        this.timer = Timer()
    }


    //private mover : Mover3D
    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
    }

    override fun onDrawFrame(gl: GL10) {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)


        var camera: Camera


        if (mover!=null && mover.getPlotter3D().isActive())
            camera = mover.getPositionMobile().calcCameraMobile()
        else
            camera = mover.getPositionMobile().calcCamera()

        gl.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)



        camera = Camera()

        val pos = camera.eye
        val dir = camera.lookat.moins(pos).norme1()
        var diff = dir.moins(pos).norme1()
        var up = camera.verticale.elem


        var posCam = pos!! //.moins(dir.norme1());

        val vertical = camera.verticale.elem.norme1()
        val vert2 = vertical.prodVect(dir).mult(-1.0)

        posCam = posCam.plus(camera.lookat.moins(posCam).mult(-0.05))

        up = dir.prodVect(up.prodVect(dir))

        GLU.gluLookAt(gl, posCam[0].toFloat(), posCam[1].toFloat(), posCam[2].toFloat(),
            dir[0].toFloat(), dir[1].toFloat(), dir[2].toFloat(),
            up[0].toFloat(), up[1].toFloat(), up[2].toFloat())



        if (circuit == null) circuit = mover.getCircuit()
        //if (circuit != null) draw(circuit as TRIConteneur, glu)


        if (toggleMenu == null) return
        if (toggleMenu.isDisplayBonus()) {
            bonus.getListRepresentable().forEach(Consumer { representable: Representable ->
                val center = (representable as TRISphere2<*>).coords
                (representable as TRISphere2<*>).circle.axis.elem.center = terrain.p3(center)
            })
            draw(bonus, glu)
        }
        if (toggleMenu.isDisplaySky()) draw(Ciel().bleu, glu)

        if (toggleMenu.isDisplayGroundGrid()) draw(terrain, glu)
        if (toggleMenu.isDisplayGround()) {
            if (terrain.isDessineMurs()) {
                displayGround(glu)
            }
        }
        if (toggleMenu.isDisplayArcs() && SolPlan::class.java == getLevel()) {
            displayArcs(glu)
        }
        if (toggleMenu.isDisplayCharacter()) {
            val `object`: Cube = vaisseau.getObject()
            `object`.setPosition(mover.calcCposition())
            draw(`object` as Cube, glu)
            if (getPlotter3D() != null && getPlotter3D().isActive()) {
                val courbeParametriquePolynomiale: CourbeParametriquePolynomiale? = null
            }
        }
/*
        if (toggleMenu.isDisplayScore()) draw(
            "Score :  " + mover.score(),
            java.awt.Color.WHITE,
            glu,
            gl
        )
  */
        /*
        if (toggleMenu.isDisplayEnergy()) draw(
            "Life :  " + mover.energy(),
            java.awt.Dimension(30, 10),
            java.awt.Color.GREEN,
            glu,
            gl
        )
*/

        drawToggleMenu(glu)

        drawTrajectory(getPlotter3D(), glu)

    }

    private fun drawToggleMenu(glu: GLU) {

    }

    private fun getLevel(): Any? {
        TODO("Not yet implemented")
    }

    private fun getPlotter3D(): Plotter3D {
        return plotter3D
    }

    override fun onSurfaceChanged(unused: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }


    fun color(gl: GLES20, c: android.graphics.Color) {
        return GLES11.glColor4f((c.red() / 255f).toFloat(), (c.green() / 255f).toFloat(), (c.blue() / 255f).toFloat(), 0.5f.toFloat())
    }
/*
    private fun draw(segd: LineSegment, glu: GLU?, gl: GLES20) {
        glu.glBegin(GLES20.GL_LINES)
        val p1 = getTerrain().p3(segd.origine)
        val p2 = getTerrain().p3(segd.extremite)
        color(gl, Color.color(segd.texture().getColorAt(0.5, 0.5)))
        gl.glVertex3f(
            p1[0].toFloat(),
            p1[1].toFloat(),
            p1[2].toFloat()
        )
        gl.glVertex3f(
            p2[0].toFloat(),
            p2[1].toFloat(),
            p2[2].toFloat()
        )
        GLES20.glEnd()
    }
*/
    fun draw(tri: TRI, glu: GLU) {
        val wallCoords : FloatArray = FloatArray(12)
        val bb: ByteBuffer = ByteBuffer.allocateDirect(3*4)
        // (number of coordinate values * 4 bytes per float) wallCoords.length * 4
        var j:Int = 0
        for (sommet in tri!!.sommet.getData1d()) {
            //ByteBuffer
            val p = getTerrain().p3(sommet)
            for(i in 0..2) {
                wallCoords[j+i*4] = sommet.get(i).toFloat()
            }
            j++
        }
// use the device hardware's native byte order
// use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())
// create a floating point buffer from the ByteBuffer
// create a floating point buffer from the ByteBuffer
        val vertexBuffer: FloatBuffer = bb.asFloatBuffer()
// add the coordinates to the FloatBuffer
        vertexBuffer.put(wallCoords)
// set the buffer to read the first coordinate
// set the buffer to read the first coordinate
        vertexBuffer.position(0)

    }

    fun draw2(tri: TRI, glu: GLU, guard: Boolean) {
        val wallCoords: FloatArray = FloatArray(12)
        val bb: ByteBuffer = ByteBuffer.allocateDirect(3 * 4)


        if (!guard);
        var i = 0
        for (sommet in tri.sommet.getData1d()) {
            wallCoords[i++] = sommet.get(0).toFloat()
            wallCoords[i++] = sommet.get(1).toFloat()
            wallCoords[i++] = sommet.get(2).toFloat()
        }
        if (!guard);


// use the device hardware's native byte order
// use the device hardware's native byte order
        bb.order(ByteOrder.nativeOrder())
// create a floating point buffer from the ByteBuffer
// create a floating point buffer from the ByteBuffer
        val vertexBuffer: FloatBuffer = bb.asFloatBuffer()
// add the coordinates to the FloatBuffer
        vertexBuffer.put(wallCoords)
// set the buffer to read the first coordinate
// set the buffer to read the first coordinate
        vertexBuffer.position(0)
    }

    fun draw(s: TRIObjetGenerateur, glu: GLU) {
        for (i in 0 until s.maxX) {
            for (j in 0 until s.maxY) {
                val tris = arrayOfNulls<TRI>(2)
                val INFINI = Point3D.INFINI
                tris[0] = TRI(INFINI, INFINI, INFINI)
                tris[1] = TRI(INFINI, INFINI, INFINI)
                s.getTris(i, j, tris)
                tris[0]?.let { draw(it, glu) }
                tris[1]?.let { draw(it, glu) }
            }
        }
    }

    open fun draw2(s: TRIObjetGenerateur, glu: GLU) {
        for (i in 0 until s.maxX) {
            for (j in 0 until s.maxY) {
                val tris = arrayOfNulls<TRI>(2)
                val INFINI = Point3D.INFINI
                tris[0] = TRI(INFINI, INFINI, INFINI)
                tris[1] = TRI(INFINI, INFINI, INFINI)
                s.getTris(i, j, tris)
                tris[0]?.let { draw(it, glu) }
                tris[1]?.let { draw(it, glu) }
            }
        }
    }

    open fun draw(gen: TRIGenerable, glu: GLU) {
        draw(gen.generate(), glu)
    }

    open fun draw(gen: TRIObject, glu: GLU) {
        gen.triangles.forEach(Consumer { t: TRI -> draw(t, glu) })
    }

    @Synchronized
    open fun draw(rc: RepresentableConteneur, glu: GLU) {
        val it = rc.iterator()
        while (it.hasNext()) {
            var r: Representable? = null
            try {
                r = it.next()
                if (r is TRI) {
                    draw(r as TRI, glu)
                } else if (r is LineSegment) {
                    draw(r as ParametricCurve, glu)
                } else if (r is ParametricSurface) {
                    draw(r as ParametricSurface, glu)
                }
            } catch (ex: ConcurrentModificationException) {
                ex.printStackTrace()
                break
            }
        }
    }

    fun draw(s: ParametricSurface, glu: GLU) {
        var i:Double = 0.0
        var j:Double = 0.0
        val incrA : Int = (1.0/(s.incrU/(s.endU-s.startU))).toInt()
        val incrB : Int = (1.0/(s.incrV/(s.endV-s.startV))).toInt()
        for (a in 0..incrA) {
            for (b in 0..incrB) {
                i = a/incrA.toDouble()
                i = b/incrB.toDouble()
                val elementSurface = s.getElementSurface(i, s.incrU, j, s.incrV)
                val INFINI = Point3D.INFINI
                draw2(
                    TRI(
                        elementSurface.points.getElem(0),
                        elementSurface.points.getElem(1),
                        elementSurface.points.getElem(2), s.texture()
                    ), glu, true
                )
                draw2(
                    TRI(
                        elementSurface.points.getElem(2),
                        elementSurface.points.getElem(3),
                        elementSurface.points.getElem(0), s.texture()
                    ),
                    glu, true
                )
            }
        }
    }

    fun draw(t: Cube, s: ParametricSurface, glu: GLU) {
        var i:Double = 0.0
        var j:Double = 0.0
        val incrA : Int = (1.0/(s.incrU/(s.endU-s.startU))).toInt()
        val incrB : Int = (1.0/(s.incrV/(s.endV-s.startV))).toInt()
        for (a in 0..incrA) {
            for (b in 0..incrB) {
                i = a/incrA.toDouble()
                i = b/incrB.toDouble()
                val elementSurface = s.getElementSurface(i, s.incrU, j, s.incrV)
                val INFINI = Point3D.INFINI
                draw2(
                    TRI(
                        terrain.p3(elementSurface.points.getElem(0)),
                        terrain.p3(elementSurface.points.getElem(1)),
                        terrain.p3(elementSurface.points.getElem(2))
                    ), glu, true
                )
                draw2(
                    TRI(
                        terrain.p3(elementSurface.points.getElem(2)),
                        terrain.p3(elementSurface.points.getElem(3)),
                        terrain.p3(elementSurface.points.getElem(0))
                    ), glu, true
                )
            }
        }
    }


    fun draw(con: TRIConteneur, glu: GLU) {
        /*if(con.getObj()==null && con instanceof TRIGenerable)
         {
         ((TRIGenerable)con).generate();
         }*/
        val iterable = con.iterable()
        iterable.forEach(Consumer { t: TRI -> draw(t, glu) })
    }

    fun draw(c: Cube, glu: GLU) {
        val generate = c.generate()
        draw(generate, glu)
    }

    fun displayArcs(glu: GLU) {
        val arc = arrayOf(
            arrayOf(
                P.n(0.5, 0.5, 0.0),
                P.n(0.5 + 0.25, 0.5 + 0.75, 0.5),
                P.n(0.5 + 0.75, 0.5 + 0.25, 0.5),
                P.n(0.5 + 1, 0.5, 0.0)
            ), arrayOf(
                P.n(0.5 + 1, 0.5, 0.0),
                P.n(0.5 + 1, 0.5 + 0.25, 0.5),
                P.n(0.5 + 1, 0.5 + 0.75, 0.5),
                P.n(0.5 + 1, 0.5 + 1, 0.0)
            )
        )

//        TubulaireN2<CourbeParametriquePolynomialeBezier> courbeParametriquePolynomialeBezierTubulaireN2 = new TubulaireN2<>();
//        courbeParametriquePolynomialeBezierTubulaireN2.curve(new CourbeParametriquePolynomialeBezier(arc[0]));
//        courbeParametriquePolynomialeBezierTubulaireN2.texture(new ColorTexture(Color.GREEN));
//
//
//        TubulaireN2<CourbeParametriquePolynomialeBezier> courbeParametriquePolynomialeBezierTubulaireN22 = new TubulaireN2<>();
//        courbeParametriquePolynomialeBezierTubulaireN22.curve(new CourbeParametriquePolynomialeBezier(arc[1]));
//        courbeParametriquePolynomialeBezierTubulaireN22.texture(new ColorTexture(Color.GREEN));

        // TODO draw(courbeParametriquePolynomialeBezierTubulaireN2, glu, gl);
        // TODO draw(courbeParametriquePolynomialeBezierTubulaireN22, glu, gl);
        }

    fun displayTerrain(glu: GLU) {
        draw(terrain as RepresentableConteneur, glu)
    }


    fun displayGround(glu: GLU) {
        var nbrTriReduce = 0
        val maxDistance = 0.01
        var i = 0.0
        while (i <= 1) {
            var j = 0.0
            while (j <= 1) {
                val faces = Cube.getData()
                val tris = arrayOfNulls<TRI>(12)
                //tris[12] = new TRI(new Point3D(0, 1, 0), new Point3D(1, 1, 0), new Point3D(0, 0, 0));
                //tris[13] = new TRI(new Point3D(1, 0, 0), new Point3D(1, 1, 0), new Point3D(0, 0, 0));
                var index = 0
                val a = 0
                for (triRaw in faces) {
                    tris[index] = TRI()
                    val p1 = Point3D(
                        (triRaw[0][0] + 1) / 2,
                        (triRaw[0][1] + 1) / 2,
                        (triRaw[0][2] + 1) / 2
                    )
                    val p2 = Point3D(
                        (triRaw[1][0] + 1) / 2,
                        (triRaw[1][1] + 1) / 2,
                        (triRaw[1][2] + 1) / 2
                    )
                    val p3 = Point3D(
                        (triRaw[2][0] + 1) / 2,
                        (triRaw[2][1] + 1) / 2,
                        (triRaw[2][2] + 1) / 2
                    )
                    tris[index] = TRI(p1, p2, p3)
                    index++
                }
                index = 0
                for (t in tris) {
                    /*
                    if(index>=12)
                    {
                        INCR_AA = 0.01;

                    }
                    else
                    {
                        INCR_AA = 0.1;
                    }
                    */
                    val point3D = arrayOfNulls<Point3D>(6)
                    for (p in intArrayOf(0, 1, 2)) {
                        if (t != null && t.sommet != null && t.sommet.data1d.size >= 3) {
                            val p3 = arrayOf(
                                t.getSommet().getElem(0),
                                t.getSommet().getElem(1), t.getSommet().getElem(2)
                            )
                            for (coord in 0..2) {
                                when (coord) {
                                    0 -> {
                                        point3D[0] = Point3D(p3[0][coord], i, j)
                                        point3D[1] = Point3D(
                                            p3[0][coord], i + INCR_AA,
                                            j
                                        )
                                        point3D[2] = Point3D(
                                            p3[0][coord], i + INCR_AA,
                                            j + INCR_AA
                                        )
                                        point3D[3] = Point3D(p3[0][coord], i, j)
                                        point3D[4] = Point3D(
                                            p3[0][coord], i,
                                            j + INCR_AA
                                        )
                                        point3D[5] = Point3D(
                                            p3[0][coord], i + INCR_AA,
                                            j + INCR_AA
                                        )
                                    }

                                    1 -> {
                                        point3D[0] = Point3D(i * 2, p3[0][coord], j * 2)
                                        point3D[1] = Point3D(
                                            (i + INCR_AA) * 2,
                                            p3[0][coord], j * 2
                                        )
                                        point3D[2] = Point3D(
                                            (i + INCR_AA) * 2,
                                            p3[0][coord], (j + INCR_AA) * 2
                                        )
                                        point3D[3] = Point3D(i * 2, p3[0][coord], j * 2)
                                        point3D[4] = Point3D(i * 2, p3[0][coord], (j + INCR_AA) * 2)
                                        point3D[5] = Point3D(
                                            (i + INCR_AA) * 2,
                                            p3[0][coord], (j + INCR_AA) * 2
                                        )
                                    }

                                    2 -> {
                                        point3D[0] = Point3D(
                                            i * 2, j * 2,
                                            p3[0][coord]
                                        )
                                        point3D[1] = Point3D(
                                            (i + INCR_AA) * 2, j * 2,
                                            p3[0][coord]
                                        )
                                        point3D[2] = Point3D(
                                            (i + INCR_AA) * 2, (j + INCR_AA) * 2,
                                            p3[0][coord]
                                        )
                                        point3D[3] = Point3D(
                                            i * 2, j * 2,
                                            p3[0][coord]
                                        )
                                        point3D[4] = Point3D(
                                            i * 2, (j + INCR_AA) * 2,
                                            p3[0][coord]
                                        )
                                        point3D[5] = Point3D(
                                            (i + INCR_AA) * 2, (j + INCR_AA) * 2,
                                            p3[0][coord]
                                        )
                                    }
                                }
                                nbrTriReduce++
                                var toDraw = TRI()
                                for (g in 0..2) {
                                    toDraw.sommet.setElem(getTerrain().p3(point3D[g]), g)
                                }
                                toDraw.texture(ColorTexture(Plasma.color(i + a, j + a, time())))
                                draw2(toDraw, glu, true)
                                toDraw = TRI()
                                for (g in 0..2) {
                                    toDraw.sommet.setElem(getTerrain().p3(point3D[g + 3]), g)
                                }
                                toDraw.texture(ColorTexture(Plasma.color(i + a, j + a, time())))
                                toDraw.texture(ColorTexture(Plasma.color(i + a, j + a, time())))
                                //if(isClose(maxDistance, toDraw))
                                toDraw?.let { draw2(toDraw, glu) }
                            }
                        }
                    }
                    index++
                }
                j += INCR_AA
            }
            i += INCR_AA
        }
    }

    private fun draw2(s: TRI, glu: GLU) {

    }

    fun isClose(maxDistance: Double, toDraw: TRI): Boolean {
        return Point3D.distance(
            getTerrain().p3(toDraw.sommet.getElem(0)),
            mover.calcCposition()
        ) < maxDistance
    }


    fun drawTriLines(triCourant: TRI, glu: GLU, b: Boolean) {}


    fun draw(c: ParametricCurve, glu: GLU) {
        var d0 = c.start()
        var d = c.start()
        while (d < c.endU()) {
            draw(
                LineSegment(
                    c.calculerPoint3D(d0),
                    c.calculerPoint3D(d)
                ) as ParametricCurve, glu
            )
            d0 = d
            d += c.incrU.elem
        }
    }

    fun drawTrajectory(plotter3D: Plotter3D?, glu: GLU) {
        if (plotter3D == null) return
        val impact = plotter3D.impact
        draw(
            CourbeParametriquePolynomiale(
                arrayOf(
                    getMover().calcCposition(),
                    getTerrain().calcCposition(impact.x, impact.y)
                )
            ), glu)
    }


    open fun setLogic(m: PositionUpdate) {
        mover = m
        vaisseau = Vaisseau(mover)
        terrain = mover.terrain
        bonus = Bonus()
        mover.ennemi(bonus)
    }

    fun time(): Double {
        return timer.getTimeEllapsed()
    }

    open fun getGlu(): GLU? {
        return glu
    }

    /*
     * sets up selection mode, name stack, and projection matrix for picking. Then
     * the objects are drawn.
     */

    /*
     * sets up selection mode, name stack, and projection matrix for picking. Then
     * the objects are drawn.
     */
    open fun getMover(): PositionUpdate {
        return mover
    }
    open fun getTerrain(): Terrain {
        return terrain
    }
/*
    open fun getBonus(): Bonus? {
        return bonus
    }

    open fun getRenderer(): TextRenderer? {
        return renderer
    }

    open fun getVaisseau(): Vaisseau? {
        return vaisseau
    }

    open fun isLocked(): Boolean {
        return locked
    }

    private open fun setLocked(l: Boolean) {
        locked = l
    }

    open fun getCircuit(): Circuit? {
        return circuit
    }

    open fun getTimer(): Timer? {
        return timer
    }

    open fun getGlcanvas(): GLCanvas? {
        return glCanvas
    }

    open fun getBUFSIZE(): Int {
        return BUFSIZE
    }

    open fun getPickPoint(): Point2D? {
        return pickPoint
    }

    open fun getPiloteAuto(): PiloteAuto? {
        return piloteAuto
    }

    open fun getPlotter3D(): Plotter3D? {
        return plotter3D
    }

    open fun setPlotter3D(plotter3D: Plotter3D) {
        plotter3D = plotter3D
    }

    open fun getAnimator(): Animator? {
        return animator
    }


    private open fun isRunning(): Boolean {
        return true
    }
*/
}



