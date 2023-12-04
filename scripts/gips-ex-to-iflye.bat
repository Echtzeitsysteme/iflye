:: This script copies the generated runtime components from the
:: (external) GIPS MdVNE example folder into this repository.

setlocal

set MDVNE_PROJECT_NAME=org.emoflon.gips.gipsl.examples.mdvne

rmdir ..\%MDVNE_PROJECT_NAME%\src-gen /s /q
xcopy ..\..\gips-examples\%MDVNE_PROJECT_NAME%\src-gen ..\%MDVNE_PROJECT_NAME%\src-gen\ /e

endlocal