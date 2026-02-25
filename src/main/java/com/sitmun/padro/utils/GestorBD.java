package com.sitmun.padro.utils;

import com.sitmun.padro.dto.DomiciliosModel;
import com.sitmun.padro.dto.ViasModel;
import com.sitmun.padro.dto.ViviendaModel;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class GestorBD {
    private static final String connDriver = "oracle.jdbc.driver.OracleDriver";
    private String connURL = "";
    private String connUser = "";
    private String connPassword = "";

    /**
     * Inicializa el GestorBD
     */
    public GestorBD(String url, String user, String pwd) {
        connURL = url;
        connUser = user;
        connPassword = pwd;
    }

    public int ejecutarProcesoVias(String municipio, List<ViasModel> list_vm) {
        return this.ejecutarProcesoVias(municipio, null, list_vm);
    }

    /**
     * M�todo para eliminar la(s) via(s) de un municipio pasado por pa�metro
     * y devuelve 1 proceso correcto, -1 proceso incorrecto
     *
     * @param municipio  Codigo del municipio a eliminar vieja(s) via(s) e inserir la(s) nueva(s)
     * @param codiIneVia Opcional, codigoIne de la nueva via
     * @return                1 proceso correcto, -1 proceso incorrecto
     */
    public int ejecutarProcesoVias(String municipio, Integer codiIneVia, List<ViasModel> list_vm) {
        Connection conn = getDBConnection(connURL, connUser, connPassword);
        try {
            //1. Eliminar via(s) vieja(s)
            String delete_statement = "DELETE FROM SIT_CAE1MV1_201X_AYTOS WHERE COD_MUNI = ?";
            if (codiIneVia != null) {
                delete_statement += " AND COD_INE_VIA = ?";
            }
            PreparedStatement ps_del = conn.prepareStatement(delete_statement);
            ps_del.setString(1, municipio);
            if (codiIneVia != null) {
                ps_del.setInt(2, codiIneVia);
            }
            ps_del.executeUpdate();
            String insert_statement = "INSERT INTO SIT_CAE1MV1_201X_AYTOS (COD_MUNI, COD_VIA, COD_INE_VIA, NOM_VIA, SIT_VIA, COD_TIP_VIA, NOM_TIP_VIA, COD_NUCLI, COD_INE_NUCLI, NOM_NUCLI) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps_insert = conn.prepareStatement(insert_statement);
            for (ViasModel vm : list_vm) {

                if (municipio == null || vm.getCodigoVia() == null || vm.getCodigoIneVia() == null ||
                        vm.getNombreVia() == null || vm.getSituacionVia() == null || vm.getCodigoTipoVia() == null || vm.getNombreTipoVia() == null
                        || vm.getCodigoNucleo() == null || vm.getCodigoIneNucleo() == null || vm.getNombreNucleo() == null) {
                    log.debug("Registro con valores a null: ");
                    log.debug("municipio" + municipio);
                    log.debug("codigo via: " + vm.getCodigoVia());
                    log.debug("codigo ine via: " + vm.getCodigoIneVia());
                    log.debug("Nombre:" + vm.getNombreVia());
                    log.debug("Situacion: " + vm.getSituacionVia());
                    log.debug("Tipo: " + vm.getCodigoTipoVia());
                    log.debug("Codigo nucleo: " + vm.getCodigoNucleo());
                    log.debug("Codigo ine nucleo:" + vm.getCodigoIneNucleo());
                    log.debug("Nombre nuco: " + vm.getNombreNucleo());
                }
                if (municipio != null) ps_insert.setString(1, municipio);
                else ps_insert.setNull(1, Types.VARCHAR);
                if (vm.getCodigoVia() != null) ps_insert.setInt(2, vm.getCodigoVia());
                else ps_insert.setNull(2, Types.INTEGER);
                if (vm.getCodigoIneVia() != null) ps_insert.setInt(3, vm.getCodigoIneVia());
                else ps_insert.setNull(3, Types.INTEGER);
                if (vm.getNombreVia() != null) ps_insert.setString(4, vm.getNombreVia());
                else ps_insert.setNull(4, Types.VARCHAR);
                if (vm.getSituacionVia() != null) ps_insert.setString(5, vm.getSituacionVia());
                else ps_insert.setNull(5, Types.VARCHAR);
                if (vm.getCodigoTipoVia() != null) ps_insert.setString(6, vm.getCodigoTipoVia());
                else ps_insert.setNull(6, Types.VARCHAR);
                if (vm.getNombreTipoVia() != null) ps_insert.setString(7, vm.getNombreTipoVia());
                else ps_insert.setNull(7, Types.VARCHAR);
                if (vm.getCodigoNucleo() != null) ps_insert.setInt(8, vm.getCodigoNucleo());
                else ps_insert.setNull(8, Types.INTEGER);
                if (vm.getCodigoIneNucleo() != null) ps_insert.setInt(9, vm.getCodigoIneNucleo());
                else ps_insert.setNull(9, Types.INTEGER);
                if (vm.getNombreNucleo() != null) ps_insert.setString(10, vm.getNombreNucleo());
                else ps_insert.setNull(10, Types.VARCHAR);

                ps_insert.addBatch();
            }
            ps_insert.executeBatch();

            conn.commit();
            return 1;
        } catch (Exception ex) {
            log.error("Error ejecutando proceso de actualizar vias del municipio " + municipio + ": " + ex.getMessage());
            try {
                conn.rollback();
            } catch (Exception e) {
                log.error("Error haciendo  rollback", e);
            }
            return -1;
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                log.error("Error cerrando conexi�n despues de inserir nuevas vias", ex);
            }
        }
    }


    /**
     * Metodo que elimina los valores viejos de la tabla domicilis_aytos e insiere los nuevos valores
     * y devuelve 1 proceso correcto, -1 proceso incorrecto
     *
     * @param municipio Codigo del municipio a eliminar viejo(s) domicilio(s) e inserir lo(s) nuevo(s)
     * @param list_dm   Lista de lo(s) nuevo(s) domicilios
     * @return                1 proceso correcto, -1 proceso incorrecto
     */
    public int ejecutarProcesoDivisionHorizontal(String municipio, List<DomiciliosModel> list_dm) {
        Connection conn = getDBConnection(connURL, connUser, connPassword);
        try {
            //1. Eliminar domicilio(s) viejo(s)
            String delete_statement = "DELETE FROM DOMICILIS_AYTOS WHERE COD_MUNI = ?";

            PreparedStatement ps_del = conn.prepareStatement(delete_statement);
            ps_del.setString(1, municipio);
            ps_del.executeUpdate();
            String insert_statement = "INSERT INTO DOMICILIS_AYTOS (COD_MUNI,COD_DOM, COD_INE_NUCLI, NOM_NUCLI, COD_INE_VIA, NOM_VIA, NUMERO_DESDE, LETRA_DESDE, NUMERO_HASTA, LETRA_HASTA, COD_BLOC, COD_PORTAL, COD_ESCALA, NOM_ESCALA, COD_PLANTA, NOM_PLANTA, COD_PUERTA, REF_CAD ) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps_insert = conn.prepareStatement(insert_statement);
            for (DomiciliosModel dm : list_dm) {

                if (municipio != null) ps_insert.setString(1, municipio);
                else ps_insert.setNull(1, Types.VARCHAR);
                if (dm.getCodigoDomicilio() != null) ps_insert.setInt(2, Integer.parseInt(dm.getCodigoDomicilio()));
                else ps_insert.setNull(2, Types.INTEGER);
                if (dm.getCodigoIneNucleo() != null) ps_insert.setInt(3, dm.getCodigoIneNucleo());
                else ps_insert.setNull(3, Types.INTEGER);
                if (dm.getNombreNucleo() != null) ps_insert.setString(4, dm.getNombreNucleo());
                else ps_insert.setNull(4, Types.VARCHAR);
                if (dm.getCodigoIneVia() != null) ps_insert.setInt(5, Integer.parseInt(dm.getCodigoIneVia()));
                else ps_insert.setNull(5, Types.INTEGER);
                if (dm.getNombreVia() != null) ps_insert.setString(6, dm.getNombreVia());
                else ps_insert.setNull(6, Types.VARCHAR);
                if (dm.getNumeroDesde() != null && !dm.getNumeroDesde().isEmpty())
                    ps_insert.setInt(7, Integer.parseInt(dm.getNumeroDesde()));
                else ps_insert.setNull(7, Types.INTEGER);
                if (dm.getLetraDesde() != null) ps_insert.setString(8, dm.getLetraDesde());
                else ps_insert.setNull(8, Types.VARCHAR);
                if (dm.getNumeroHasta() != null && !dm.getNumeroHasta().isEmpty())
                    ps_insert.setInt(9, Integer.parseInt(dm.getNumeroHasta()));
                else ps_insert.setNull(9, Types.INTEGER);
                if (dm.getLetraHasta() != null) ps_insert.setString(10, dm.getLetraHasta());
                else ps_insert.setNull(10, Types.VARCHAR);

                if (dm.getCodigoBloque() != null) ps_insert.setString(11, dm.getCodigoBloque());
                else ps_insert.setNull(11, Types.VARCHAR);
                if (dm.getCodigoPortal() != null) ps_insert.setString(12, dm.getCodigoPortal());
                else ps_insert.setNull(12, Types.VARCHAR);
                if (dm.getCodigoEscalera() != null) ps_insert.setString(13, dm.getCodigoEscalera());
                else ps_insert.setNull(13, Types.VARCHAR);
                if (dm.getNombreEscalera() != null) ps_insert.setString(14, dm.getNombreEscalera());
                else ps_insert.setNull(14, Types.VARCHAR);
                if (dm.getCodigoPlanta() != null) ps_insert.setString(15, dm.getCodigoPlanta());
                else ps_insert.setNull(15, Types.VARCHAR);
                if (dm.getNombrePlanta() != null) ps_insert.setString(16, dm.getNombrePlanta());
                else ps_insert.setNull(16, Types.VARCHAR);
                if (dm.getCodigoPuerta() != null) ps_insert.setString(17, dm.getCodigoPuerta());
                else ps_insert.setNull(17, Types.VARCHAR);
                if (dm.getReferenciaCatastral() != null)
                    ps_insert.setString(18, dm.getReferenciaCatastral());
                else ps_insert.setNull(18, Types.VARCHAR);

                ps_insert.addBatch();
            }
            ps_insert.executeBatch();

            conn.commit();
            return 1;
        } catch (Exception ex) {
            log.error("Error ejecutando proceso de actualizar domicilios del municipio " + municipio + ": " + ex.getMessage());
            try {
                conn.rollback();
            } catch (Exception e) {
                log.error("Error haciendo  rollback", e);
            }
            return -1;
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                log.error("Error cerrando conexi�n despues de inserir nuevos domicilios", ex);
            }
        }
    }

    /**
     * M�todo para importar de la tabla domicilis_aytos a la tabla SIT_CAE1MV1_204x  de callejero de sitmun donde est�n las viviendas de un portal
     * y devuelve 1 proceso correcto, -1 proceso incorrecto
     *
     * @param municipio Codigo del municipio a eliminar vieja(s) via(s) e inserir la(s) nueva(s)
     * @return                1 proceso correcto, -1 proceso incorrecto
     */
    public int ejecutarProcesoViviendas(String municipio) {
        Connection conn = getDBConnection(connURL, connUser, connPassword);
        try {
            //1. Eliminar domicilio(s) viejo(s)
        		/*String delete_statement = "DELETE FROM SIT_CAE1MV1_204X WHERE COD_MUNI = ?";
        		PreparedStatement ps_del = conn.prepareStatement(delete_statement);
        		ps_del.setString(1,municipio);
        		ps_del.executeUpdate();*/
            String insert_statement =
                    "insert into sit_cae1mv1_204x (ID, COD_MUNI, COD_S_ADR_VIA, COD_ADR_VIA, ESCALA, PLANTA, PORTA, VALID_DE, DATA_CREA, ED_USUARI) " +
                            "select num_id.last_id+rownum, aytos.cod_muni, num_cod_s_adr_via.last_codi+rownum, sitmun.cod_adr_via, aytos.cod_escala, " +
                            "       nvl(codis_plantes.t_abreujat, aytos.cod_planta), aytos.cod_puerta, sysdate as \"valid_de\", sysdate as \"data_crea\", 'aytos' as \"ed_usuari\" " +
                            "from (select nvl(max(id), 0) as last_id from sit_cae1mv1_204x) num_id, " +
                            "     (select nvl(max(COD_S_ADR_VIA), 0) as last_codi from sit_cae1mv1_204x where cod_muni=?) num_cod_s_adr_via, " +
                            "      domicilis_aytos aytos " +
                            "          left outer join sit_cae1mv1_210x codis_plantes " +
                            "          on aytos.cod_planta = codis_plantes.T_ABREUJAT_AYTOS " +
                            "          inner join ( " +
                            "            select portal.cod_muni, portal.num_ini, portal.cod_adr_via, ine.cod_ex, via.tip_via || ' ' || via.nexe_via || ' ' || via.nom_via as nom_via, via.cod_via " +
                            "           from sit_cae1mv1_122p portal " +
                            "            inner join sit_cae1mv1_112x1 via " +
                            "            on portal.cod_muni = via.cod_muni and portal.cod_via = via.cod_via " +
                            "            inner join sit_cae1mv1_201x ine " +
                            "            on ine.cod_muni = via.cod_muni and ine.cod_via = via.cod_via and ine.tip_var='nomINE') sitmun " +
                            "          on aytos.cod_muni = sitmun.cod_muni and to_number(aytos.COD_INE_VIA) = to_number(sitmun.cod_ex) and aytos.numero_desde = sitmun.num_ini " +
                            "          where aytos.cod_muni=? and aytos.numero_desde is not null " +
                            "          order by aytos.cod_ine_via, numero_desde ";

            PreparedStatement ps_insert = conn.prepareStatement(insert_statement);
            if (municipio != null) {
                ps_insert.setString(1, municipio);
                ps_insert.setString(2, municipio);
            } else {
                ps_insert.setNull(1, Types.VARCHAR);
                ps_insert.setNull(2, Types.VARCHAR);
            }

            ps_insert.executeUpdate();
            conn.commit();
            return 1;
        } catch (Exception ex) {
            log.error("Error ejecutando proceso de actualizar viviendas del municipio " + municipio + ": " + ex.getMessage());
            try {
                conn.rollback();
            } catch (Exception e) {
                log.error("Error haciendo  rollback", e);
            }
            return -1;
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                log.error("Error cerrando conexi�n despues de inserir nuevas viviendas", ex);
            }
        }
    }

    /**
     * M�todo para el ws de aytos que segun los parametros de entrada devuelve portales/edifico o la vertical
     *
     */
    public List<ViviendaModel> consultaHabitants(String control, HashMap<String, String> params, String url) {

        Connection conn = getDBConnection(connURL, connUser, connPassword);
        try {
            List<ViviendaModel> result = new ArrayList<ViviendaModel>();

            //Si la consulta es PMH/PL i ESCALA ES NOT NULL HEM DE FER LA CONVERSIO a la taula

            if (control.equalsIgnoreCase("PL") && params.get("planta") != null && params.get("planta").length() > 0) {
                PreparedStatement ps_consulta_planta = conn.prepareStatement("SELECT T_ABREUJAT from SIT_CAE1MV1_210X where T_ABREUJAT_AYTOS=?");
                log.debug("Busquem planta de conversio per a : " + params.get("planta"));
                ps_consulta_planta.setString(1, params.get("planta").toUpperCase());
                ResultSet resultset_planta = ps_consulta_planta.executeQuery();
                while (resultset_planta.next()) {
                    params.put("planta", resultset_planta.getString("T_ABREUJAT"));
                    break;
                }
            }

            ResultSet resultset = executarConsulta(control, params, conn);

            log.debug("Consulta executada correctament. Tornem elements");

            String refcad = "";
            String mun_ine = "";

            while (resultset.next()) {
                ViviendaModel vivienda = new ViviendaModel();


                vivienda.setExiste(true);

                refcad = resultset.getString("REF_CAD");
                mun_ine = resultset.getString("COD_MUNI");

                if (refcad != null && refcad.length() > 14) {
                    vivienda.setReferenciaCatastral20(String.valueOf(refcad)); //Fem la copia abans de tallarla
                }

                if (refcad != null && refcad.length() >= 14) {
                    refcad = refcad.substring(0, 14);
                }

                vivienda.setReferenciaCatastral(refcad);
                vivienda.setCodigoMunicipio(mun_ine);
                vivienda.setCodigoNucleo(resultset.getString("UPOB_INE7"));
                vivienda.setNombreNucleo(resultset.getString("NOM_UPOB_INE"));
                vivienda.setCodigoBdmac(resultset.getString("IDICC"));
                vivienda.setCodigoIne(resultset.getString("COD_INE"));
                vivienda.setNombre(resultset.getString("NOM"));
                vivienda.setNumero(resultset.getString("NUMERO"));
                vivienda.setComplemento(resultset.getString("COMPLEMENTO"));
                vivienda.setBloque(resultset.getString("BLOQUE"));
                vivienda.setCBloque(resultset.getString("CBLOQUE"));
                vivienda.setEscalera(resultset.getString("ESCALERA"));
                vivienda.setPlanta(resultset.getString("PLANTA"));
                vivienda.setPuerta(resultset.getString("PUERTA"));


                if (refcad == null) refcad = ""; //Si la referencia catastral es null que no peti la linia de sota
                url = url.replace("{refcad}", refcad);
                url = url.replace("{mun_ine}", mun_ine);

                vivienda.setUrl(url);

                if (vivienda.getEscalera() != null) vivienda.setEscalera(vivienda.getEscalera().trim());
                if (vivienda.getPlanta() != null) vivienda.setPlanta(vivienda.getPlanta().trim());
                if (vivienda.getPuerta() != null) vivienda.setPuerta(vivienda.getPuerta().trim());

                vivienda.setCoord_x(resultset.getBigDecimal("X"));
                vivienda.setCoord_y(resultset.getBigDecimal("Y"));

                result.add(vivienda);
            }

            return result;
        } catch (Exception ex) {
            log.error("Error ejecutando recuperacion de viviendas. Los parametros de la consulta son: " + params.toString());
            try {
                conn.rollback();
            } catch (Exception e) {
                log.error("Error haciendo  rollback", e);
            }
            return null;
        } finally {
            try {
                conn.close();
            } catch (Exception ex) {
                log.error("Error cerrando conexi�n despues de inserir nuevas viviendas", ex);
            }
        }
    }

    private ResultSet executarConsulta(String control, HashMap<String, String> params, Connection conn) throws SQLException {

        log.debug("init del executarConsulta");
        log.debug("control: " + control);
        log.debug("params: " + params.toString());


        String consulta_stament = null;
        List<String> where = new ArrayList<String>();

        //1. Cerca per portals amb codi via ine
        if (control.equalsIgnoreCase("PT") && params.get("cod_via_ine") != null) {

            consulta_stament =
                    "select p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine," +
                            " via.idicc, via.tip_via || ' '  || via.nexe_via  || ' ' || via.nom_via as nom," +
                            " via_ine.cod_ex as cod_ine," +
                            " p.num_ini || " +
                            " (CASE WHEN p.num_fi IS NOT NULL THEN  '-' ELSE  '' END) || " +
                            " (CASE WHEN p.num_fi IS NOT NULL THEN  cast(p.num_fi as varchar2(20)) ELSE  '' END)" +
                            " as numero," +
                            " p.com_ini as complemento,p.bloc as bloque, p.com_bloc as cbloque, " +
                            " null as escalera, null as planta, null as puerta," +
                            " p.ref_cad as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_122P p" +
                            " inner join SIT_CAE1MV1_112X1 via" +
                            " on p.cod_via = via.cod_via and p.cod_unit_p = via.cod_unit_p and p.cod_muni = via.cod_muni" +
                            " left outer join SIT_CAE1MV1_201X via_ine" +
                            " on via.cod_via = via_ine.cod_via and via.cod_muni = via_ine.cod_muni and via_ine.tip_var='nomINE'" +
                            " inner join SIT_DTE50V1_113X u" +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE" +
                            " where ${where}";

            log.debug("Abans de subtituir el where" + consulta_stament);

            if (params.get("mun_ine") != null) where.add("p.COD_MUNI='" + params.get("mun_ine") + "'");
            if (params.get("cod_upob_ine") != null) where.add("p.COD_UNIT_P ='" + params.get("cod_upob_ine") + "'");
            if (params.get("cod_via_ine") != null)
                where.add("TO_NUMBER(via_ine.COD_EX) =" + Long.parseLong(params.get("cod_via_ine")));
            if (params.get("numero") != null) where.add("p.NUM_INI =" + params.get("numero"));
            if (params.get("complemento") != null && params.get("complemento").length() > 0)
                where.add("upper(p.COM_INI) like '%" + params.get("complemento").toUpperCase() + "%'");
            else where.add("(TRIM(p.COM_INI) IS NULL OR p.COM_INI is null)");
            if (params.get("bloque") != null && params.get("bloque").length() > 0)
                where.add("upper(p.BLOC) like '%" + params.get("bloque").toUpperCase() + "%'");
            else where.add("(TRIM(p.BLOC) IS NULL OR p.BLOC is null)");
            if (params.get("cbloque") != null && params.get("cbloque").length() > 0)
                where.add("upper(p.COM_BLOC) like '%" + params.get("cbloque").toUpperCase() + "%'");
            else where.add("(TRIM(p.COM_BLOC) IS NULL OR p.COM_BLOC is null)");

        }

        //2. Cerca per edificis amb codi pseudovia
        else if (control.equalsIgnoreCase("PT") && params.get("cod_pse_ine") != null) {

            consulta_stament =
                    " select  p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine," +
                            " p.idicc, p.nom_edi as nom," +
                            " p.cod_ex as cod_ine," +
                            " null as numero," +
                            " p.com_adr as complemento, null as bloque, null as cbloque, " +
                            " null as escalera, null as planta, null as puerta," +
                            " p.ref_cad as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_123P p" +
                            " inner join SIT_DTE50V1_113X u" +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE" +
                            " where ${where}";

            log.debug("Abans de subtituir el where" + consulta_stament);

            if (params.get("mun_ine") != null) where.add("p.COD_MUNI='" + params.get("mun_ine") + "'");
            if (params.get("cod_upob_ine") != null) where.add("p.COD_UNIT_P ='" + params.get("cod_upob_ine") + "'");
            if (params.get("cod_pse_ine") != null)
                where.add("TO_NUMBER(p.COD_EX) =" + Long.parseLong(params.get("cod_pse_ine")));
        } else if (control.equalsIgnoreCase("PL") && params.get("cod_via_ine") != null) {

            consulta_stament =
                    " select  p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine," +
                            " via.idicc, via.tip_via || ' '  || via.nexe_via  || ' ' || via.nom_via as nom," +
                            " via_ine.cod_ex as cod_ine," +
                            " p.num_ini || " +
                            " (CASE WHEN p.num_fi IS NOT NULL THEN  '-' ELSE  '' END) || " +
                            " (CASE WHEN p.num_fi IS NOT NULL THEN  cast(p.num_fi as varchar2(20)) ELSE  '' END)" +
                            " as numero," +
                            " p.com_ini as complemento,p.bloc as bloque, p.com_bloc as cbloque, " +
                            " nvl(v.escala,' ') as escalera, nvl(v.planta,' ') as planta, nvl(v.porta,' ') as puerta," +
                            " v.refcad20 as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_204X v" +
                            " inner join SIT_CAE1MV1_122P p" +
                            " on v.cod_adr_via = p.cod_adr_via and v.cod_muni = p.cod_muni" +
                            " inner join SIT_CAE1MV1_112X1 via" +
                            " on p.cod_via = via.cod_via and p.cod_unit_p = via.cod_unit_p and p.cod_muni = via.cod_muni" +
                            " left outer join SIT_CAE1MV1_201X via_ine" +
                            " on via.cod_via = via_ine.cod_via and via.cod_muni = via_ine.cod_muni and via_ine.tip_var='nomINE'" +
                            " inner join SIT_DTE50V1_113X u" +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE" +
                            " where ${where}";

            log.debug("Abans de subtituir el where" + consulta_stament);

            if (params.get("mun_ine") != null) where.add("p.COD_MUNI='" + params.get("mun_ine") + "'");
            if (params.get("cod_upob_ine") != null) where.add("p.COD_UNIT_P ='" + params.get("cod_upob_ine") + "'");
            if (params.get("cod_via_ine") != null)
                where.add("TO_NUMBER(via_ine.COD_EX) =" + Long.parseLong(params.get("cod_via_ine")));
            if (params.get("numero") != null) where.add("p.NUM_INI =" + params.get("numero"));
            if (params.get("complemento") != null && params.get("complemento").length() > 0)
                where.add("upper(p.COM_INI) like '%" + params.get("complemento").toUpperCase() + "%'");
            else where.add("(TRIM(p.COM_INI) IS NULL OR p.COM_INI is null)");
            if (params.get("bloque") != null && params.get("bloque").length() > 0)
                where.add("upper(p.BLOC) like '%" + params.get("bloque").toUpperCase() + "%'");
            else where.add("(TRIM(p.BLOC) IS NULL OR p.BLOC is null)");
            if (params.get("cbloque") != null && params.get("cbloque").length() > 0)
                where.add("upper(p.COM_BLOC) like '%" + params.get("cbloque").toUpperCase() + "%'");
            else where.add("(TRIM(p.COM_BLOC) IS NULL OR p.COM_BLOC is null)");
            if (params.get("escalera") != null && params.get("escalera").length() > 0)
                where.add("upper(v.ESCALA) = '" + params.get("escalera").toUpperCase() + "'");
            else where.add("(TRIM(v.ESCALA) IS NULL OR v.ESCALA is null)");
            if (params.get("planta") != null && params.get("planta").length() > 0)
                where.add("upper(v.PLANTA) = '" + params.get("planta").toUpperCase() + "'");
            else where.add("(TRIM(v.PLANTA) IS NULL OR v.PLANTA is null)");
            if (params.get("puerta") != null && params.get("puerta").length() > 0)
                where.add("upper(v.PORTA) = '" + params.get("puerta").toUpperCase() + "'");
            else where.add("(TRIM(v.PORTA) IS NULL OR v.PORTA is null)");
        } else if (control.equalsIgnoreCase("PL") && params.get("cod_pse_ine") != null) {

            consulta_stament =
                    "select p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine," +
                            " p.idicc, p.nom_edi as nom," +
                            " p.cod_ex as cod_ine," +
                            " null as numero, " +
                            " p.com_adr as complemento,null as bloque, null as cbloque, " +
                            " ' ' as escalera, ' ' as planta, ' ' as puerta," +
                            " v.refcad20 as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_208X v" +
                            " inner join SIT_CAE1MV1_123P p" +
                            " on v.cod_adr_edi = p.cod_adr_edi and v.cod_muni = p.cod_muni" +
                            " inner join SIT_DTE50V1_113X u" +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE" +
                            " where ${where}";

            log.debug("Abans de subtituir el where" + consulta_stament);

            if (params.get("mun_ine") != null) where.add("p.COD_MUNI='" + params.get("mun_ine") + "'");
            if (params.get("cod_upob_ine") != null) where.add("p.COD_UNIT_P='" + params.get("cod_upob_ine") + "'");
            if (params.get("cod_pse_ine") != null)
                where.add("TO_NUMBER(p.COD_EX) =" + Long.parseLong(params.get("cod_pse_ine")));
        } else if (control.equalsIgnoreCase("CD") && params.get("ref_cat").length() == 14) {

            consulta_stament =
                    "select p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine, " +
                            " via.idicc, via.tip_via || ' '  || via.nexe_via  || ' ' || via.nom_via as nom, " +
                            " via_ine.cod_ex as cod_ine, " +
                            " p.num_ini || " +
                            " (CASE WHEN p.num_fi IS NOT NULL THEN  '-' ELSE  '' END) || " +
                            " (CASE WHEN p.num_fi IS NOT NULL THEN  cast(p.num_fi as varchar2(20)) ELSE  '' END)" +
                            " as numero, " +
                            " p.com_ini as complemento,p.bloc as bloque, p.com_bloc as cbloque, " +
                            " null as escalera, null as planta, null as puerta," +
                            " p.ref_cad as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_122P p " +
                            " inner join SIT_CAE1MV1_112X1 via " +
                            " on p.cod_via = via.cod_via and p.cod_unit_p = via.cod_unit_p and p.cod_muni = via.cod_muni " +
                            " left outer join SIT_CAE1MV1_201X via_ine " +
                            " on via.cod_via = via_ine.cod_via and via.cod_muni = via_ine.cod_muni and via_ine.tip_var='nomINE' " +
                            " inner join SIT_DTE50V1_113X u " +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE " +
                            " where ${where} " +
                            " union all " +
                            " select p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine," +
                            " p.idicc, p.nom_edi as nom," +
                            " p.cod_ex as cod_ine," +
                            " null as numero," +
                            " p.com_adr as complemento,null as bloque, null as cbloque, " +
                            " null as escalera, null as planta, null as puerta," +
                            " p.ref_cad as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_123P p" +
                            " inner join SIT_DTE50V1_113X u" +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE" +
                            " where ${where}";

            log.debug("Abans de subtituir el where" + consulta_stament);

            where.add("p.ref_cad ='" + params.get("ref_cat") + "'");

        } else if (control.equalsIgnoreCase("CD") && params.get("ref_cat").length() == 20) {
            consulta_stament =
                    "select v.cod_muni, " +
                            "u.upob_ine7, u.nom_upob_ine, " +
                            "via.idicc, via.tip_via || ' '  || via.nexe_via  || ' ' || via.nom_via as nom, " +
                            "via_ine.cod_ex as cod_ine, " +
                            "p.num_ini || " +
                            "(CASE WHEN p.num_fi IS NOT NULL THEN  '-' ELSE  '' END) || " +
                            "(CASE WHEN p.num_fi IS NOT NULL THEN  cast(p.num_fi as varchar2(20)) ELSE  '' END)" +
                            " as numero, " +
                            " p.com_ini as complemento,p.bloc as bloque, p.com_bloc as cbloque, " +
                            " nvl(v.escala,' ') as escalera, nvl(v.planta,' ') as planta, nvl(v.porta,' ') as puerta," +
                            " v.refcad20 as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_204X v " +
                            " inner join SIT_CAE1MV1_122P p " +
                            " on v.cod_adr_via = p.cod_adr_via and p.cod_muni = v.cod_muni " +
                            " inner join SIT_CAE1MV1_112X1 via " +
                            " on p.cod_via = via.cod_via and p.cod_unit_p = via.cod_unit_p and p.cod_muni = via.cod_muni " +
                            " left outer join SIT_CAE1MV1_201X via_ine " +
                            " on via.cod_via = via_ine.cod_via and via.cod_muni = via_ine.cod_muni and via_ine.tip_var='nomINE' " +
                            " inner join SIT_DTE50V1_113X u " +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE " +
                            " where ${where} " +
                            " union all " +
                            " select p.cod_muni, " +
                            " u.upob_ine7, u.nom_upob_ine," +
                            " p.idicc, p.nom_edi as nom, " +
                            " p.cod_ex as cod_ine," +
                            " null as numero," +
                            " p.com_adr as complemento,null as bloque, null as cbloque," +
                            " ' ' as escalera, ' ' as planta, ' ' as puerta," +
                            " v.refcad20 as ref_cad," +
                            " p.geom.sdo_point.x as X," +
                            " p.geom.sdo_point.y as Y" +
                            " from SIT_CAE1MV1_208X v" +
                            " inner join SIT_CAE1MV1_123P p" +
                            " on v.cod_adr_edi = p.cod_adr_edi and v.cod_muni = p.cod_muni" +
                            " inner join SIT_DTE50V1_113X u" +
                            " on p.cod_unit_p = u.upob_ine7 and p.cod_muni = u.MUN_INE" +
                            " where ${where}";

            log.debug("Abans de subtituir el where" + consulta_stament);

            where.add("v.refcad20 ='" + params.get("ref_cat") + "'");
        }


        String where_joined = "";

        for (int i = 0; i < where.size(); i++) {
            where_joined = where_joined + where.get(i);
            if (i != where.size() - 1) {
                where_joined = where_joined + " AND ";
            }
        }

        log.debug("Consulta a realitzar per vivendes abans del where:" + consulta_stament);
        log.debug("Where:" + where_joined);

        consulta_stament = consulta_stament.replace("${where}", where_joined);

        log.debug("Consulta a realitzar per vivendes:" + consulta_stament);

        PreparedStatement ps_consulta = conn.prepareStatement(consulta_stament);
        ResultSet resultset = ps_consulta.executeQuery();
        return resultset;
    }


    public String consultaRefCad(String refCad) {

        String result = "";
        Connection conn = getDBConnection(connURL, connUser, connPassword);
        try {


            //Si la consulta es PMH/PL i ESCALA ES NOT NULL HEM DE FER LA CONVERSIO a la taula
            PreparedStatement ps_consulta_refcad = conn.prepareStatement("SELECT REFCAD20, count(*) as CONT from SIT_CAE1MV1_204x where REFCAD20 LIKE ? group by REFCAD20");
            ps_consulta_refcad.setString(1, refCad + "%");

            log.debug("Consulta a realitzar per vivendes:" + "SELECT REFCAD20, count(*) as CONT from SIT_CAE1MV1_204x where REFCAD20 LIKE '" + refCad + "%'");

            ResultSet resultset_refCad = ps_consulta_refcad.executeQuery();

            int cont = 0;
            int result_cont = 0;
            while (resultset_refCad.next()) {
                cont++;
                result = resultset_refCad.getString("REFCAD20");
                result_cont = resultset_refCad.getInt("CONT");

            }

            //Si hem trobat diferents refcad de 20 o diferents registres amb la mateixa ref cad de 20 retornem un blanc per dir que cal retornar el portal
            if (cont != 1 || result_cont != 1) {
                result = "";
            }
        } catch (Exception e) {
            log.error("Error recuperant refCad", e);
            result = "";
        }
        return result;

    }

    public Connection getDBConnection() {
        return getDBConnection(connURL, connUser, connPassword);
    }

    /**
     * Devuelve una conexi�n
     *
     * @param url  url de la connexi�n
     * @param user usuario
     * @param pwd  password
     * @return connexi�n
     */
    public static Connection getDBConnection(String url, String user, String pwd) {

        boolean local = true;
        Connection conn = null;

        if ((user != null) && (user.trim().length() > 0)) {
            local = false;
        }
        try {
            try {
                if (local) {
                    conn = DriverManager.getConnection(url);
                } else {
                    conn = DriverManager.getConnection(url, user, pwd);
                }
            } catch (Exception e) { // registrar el Driver per Oracle
                try {
                    Class.forName(connDriver);
                    if (local) {
                        conn = DriverManager.getConnection(url);
                    } else {
                        conn = DriverManager.getConnection(url, user, pwd);
                    }
                } catch (ClassNotFoundException nfe) {
                    log.error("No se puede encontrar el driver de conexion", nfe);
                }
            }
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            log.error("Excepcion  SQL al obtenir la conexion", ex);
        }
        return conn;
    }

}


