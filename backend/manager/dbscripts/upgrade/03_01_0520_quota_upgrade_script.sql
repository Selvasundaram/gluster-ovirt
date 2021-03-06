CREATE OR REPLACE FUNCTION QuotaUpgradeScript_03_01_0480()
   RETURNS void
   AS $function$
   DECLARE
	cur RECORD;
	quota_uuid UUID;
	quota_limitation_uuid UUID;
	QuotaThresholdVdsGroup INTEGER;
	QuotaThresholdStorage INTEGER;
	QuotaGraceVdsGroup INTEGER;
	QuotaGraceStorage INTEGER;
   BEGIN
    -- Set Quota threshold and grace
    SELECT option_value into QuotaThresholdVdsGroup FROM vdc_options where option_name = 'QuotaThresholdVdsGroup';
    SELECT option_value into QuotaThresholdStorage FROM vdc_options where option_name = 'QuotaThresholdStorage';
    SELECT option_value into QuotaGraceVdsGroup FROM vdc_options where option_name = 'QuotaGraceVdsGroup';
    SELECT option_value into QuotaGraceStorage FROM vdc_options where option_name = 'QuotaGraceStorage';

    FOR cur IN (SELECT *
                FROM   storage_pool
                WHERE  id NOT IN (SELECT DISTINCT storage_pool_id FROM quota))
    LOOP
      quota_uuid := uuid_generate_v1();
      -- Insert unlimited Quota for storage pool
      INSERT
      INTO   quota
             (id,
              storage_pool_id,
              quota_name,
              description,
              threshold_vds_group_percentage,
              threshold_storage_percentage,
              grace_vds_group_percentage,
              grace_storage_percentage,
              is_default_quota)
      VALUES
             (quota_uuid,
              cur.id,
              'Quota_Def_' || cur.name,
              'Automatic generated Quota for Data Center ' || cur.name,
              QuotaThresholdVdsGroup,
	          QuotaThresholdStorage,
	          QuotaGraceVdsGroup,
	          QuotaGraceStorage,
	          true);

      -- Set quota limitations of unlimited Quota
      quota_limitation_uuid := uuid_generate_v1();
      INSERT
      INTO   quota_limitation
             (id,
              quota_id,
              storage_id,
              vds_group_id,
              virtual_cpu,
              mem_size_mb,
              storage_size_gb)
      VALUES
             (quota_limitation_uuid,
              quota_uuid,
              null,
              null,
              -1,
              -1,
              -1);

      -- Set Vms to consume from unlimited Quota
      UPDATE vm_static set quota_id = quota_uuid where vm_guid in (SELECT vm_static.vm_guid as vm_guid
       FROM vds_groups,vm_static
       WHERE storage_pool_id = cur.id
       AND vm_static.vds_group_id = vds_groups.vds_group_id);

      -- Set images to consume from unlimited Quota.
      UPDATE images set quota_id = quota_uuid
      WHERE image_guid in (SELECT images.image_guid as image_guid
                           FROM images,storage_pool_iso_map spim,storage_domain_static sds, image_storage_domain_map igsdm
                           WHERE sds.id = igsdm.storage_domain_id
                           AND igsdm.image_id = images.image_guid
                           AND sds.id = spim.storage_id
                           AND storage_pool_id = cur.id);

      -- Update storage pool with the enforcement 0
      UPDATE storage_pool
      SET quota_enforcement_type=0
      WHERE id = cur.id;

      insert into permissions (id,role_id,ad_element_id,object_id,object_type_id)
      values(uuid_generate_v1(),
      'def0000a-0000-0000-0000-def00000000a', -- Quota consume role
      getGlobalIds('everyone'),
      quota_uuid,    -- quota id --
      17);          -- Quota object type id --
   END LOOP;
END; $function$
LANGUAGE plpgsql;

SELECT * FROM QuotaUpgradeScript_03_01_0480();
drop function QuotaUpgradeScript_03_01_0480();
