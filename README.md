homonym_revisor
==========================================================================
����: hankersyan  
���: �����ض��ʵ�����Ľ�����У��  

����
-----------

��ָ���ʵ�Ļ����£�У�����Ľ����ʣ�֧��ģ����������Ŀ��Ӧ����ĳЩ�ض���ҵ����ߵ���������������׼ȷ�ʡ�


����
------------------------------------

		// ׼����ҵ�ʵ�
		TreeMap<String, String> map = new TreeMap<String, String>();
		map.put("abakawei", "���Ϳ�Τ");
		map.put("abeiaiershishoushu", "��������������");
		map.put("abendazuo", "��������");
		map.put("aerrezuerxuehongdanbai", "���������Ѫ�쵰��");
		map.put("xueyangbaohedu", "Ѫ�����Ͷ�");
		map.put("tangshizonghezheng", "�����ۺ�֢");

		// �������
		final String origin = ".���˵�Ѫ�����Ͷ���64%,�ϣ������������к���Σ����!";

		HomonymRevisor revisor = new HomonymRevisor(map, true);
		
		// У�����
		final String revised = revisor.revise(origin);
