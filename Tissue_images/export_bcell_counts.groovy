import qupath.lib.objects.classes.PathClassTools

def project = getProject()
def outputDir = buildFilePath(PROJECT_BASE_DIR, 'exports')
mkdirs(outputDir)

def outFile = new File(buildFilePath(outputDir, 'tissue_Bcell_summary2.csv'))
def writer = outFile.newWriter()

def headerWritten = false

project.getImageList().each { entry ->

    println "Processing: ${entry.getImageName()}"

    try {
        def imageData = entry.readImageData()
        def hierarchy = imageData.getHierarchy()
        def server = imageData.getServer()

        def pxWidth = server.getPixelCalibration().getPixelWidthMicrons()
        def pxHeight = server.getPixelCalibration().getPixelHeightMicrons()

        def imageName = entry.getImageName()

        // Tissue annotations only
        def tissueAnnotations = hierarchy.getAnnotationObjects().findAll {
            it.getPathClass() == getPathClass("Tissue")
        }

        tissueAnnotations.eachWithIndex { tissue, idx ->

            def tissueRoi = tissue.getROI()
            def tissueAreaUm2 = tissueRoi.getScaledArea(pxWidth, pxHeight)

            // Get all cell objects inside tissue annotation
            def cells = hierarchy.getCellObjects().findAll { cell ->
                def cellRoi = cell.getROI()
                cellRoi != null && tissueRoi.contains(cellRoi.getCentroidX(), cellRoi.getCentroidY())
            }

            int totalCells = 0
            int cd20Only = 0
            int cd79aOnly = 0
            int both = 0

            cells.each { cell ->
            totalCells++   // count every cell inside tissue

            def pc = cell.getPathClass()
            if (pc == null)
                return

            def parts = PathClassTools.splitNames(pc)
            def className = parts.join(": ")

            if (className == "CD20") {
                cd20Only++
            } else if (className == "CD79a") {
                cd79aOnly++
            } else if (className == "CD20: CD79a" || className == "CD79a: CD20") {
                both++
            }
            }

            def row = [
                Image: imageName,
                TissueIndex: idx + 1,
                TissueArea_um2: tissueAreaUm2,
                TotalCells: totalCells,
                CD20_Only: cd20Only,
                CD79a_Only: cd79aOnly,
                CD20_CD79a_Both: both
            ]

            if (!headerWritten) {
                writer.println(row.keySet().join(","))
                headerWritten = true
            }

            writer.println(row.values().join(","))
        }

    } catch (Exception e) {
        println "ERROR processing ${entry.getImageName()}: ${e.getMessage()}"
    }
}

writer.close()
println "Saved summary to: ${outFile}"
