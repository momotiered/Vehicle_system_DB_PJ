<template>
  <div class="material-management">
    <div class="header">
      <h1>维修材料管理</h1>
      <el-button type="primary" @click="showAddMaterialDialog">新增材料</el-button>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="search-area">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索材料名称"
        class="search-input"
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      
      <el-select v-model="categoryFilter" placeholder="材料类别" clearable @change="handleFilter">
        <el-option v-for="category in categories" :key="category" :label="category" :value="category"></el-option>
      </el-select>
      
      <el-checkbox v-model="showLowStock" @change="handleFilter">显示库存不足</el-checkbox>
    </div>

    <!-- 材料列表 -->
    <el-table
      v-loading="loading"
      :data="filteredMaterials"
      stripe
      style="width: 100%"
      :default-sort="{ prop: 'stockQuantity', order: 'ascending' }"
    >
      <el-table-column prop="materialId" label="ID" width="80" />
      <el-table-column prop="name" label="名称" min-width="180" sortable />
      <el-table-column prop="category" label="类别" width="120" sortable />
      <el-table-column label="单价" width="120" sortable prop="unitPrice">
        <template #default="scope">
          ¥{{ scope.row.unitPrice }}
        </template>
      </el-table-column>
      <el-table-column label="库存" width="150" sortable prop="stockQuantity">
        <template #default="scope">
          <span :class="{ 'low-stock': scope.row.stockQuantity <= 10 }">
            {{ scope.row.stockQuantity }} {{ scope.row.unit }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="supplier" label="供应商" min-width="150" />
      <el-table-column prop="sku" label="SKU" width="120" />
      <el-table-column label="操作" width="220">
        <template #default="scope">
          <el-button type="primary" size="small" @click="editMaterial(scope.row)">编辑</el-button>
          <el-button type="success" size="small" @click="showStockDialog(scope.row)">调整库存</el-button>
          <el-button type="danger" size="small" @click="deleteMaterial(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:currentPage="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="filteredMaterials.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增/编辑材料对话框 -->
    <el-dialog
      v-model="materialDialogVisible"
      :title="isEditing ? '编辑材料' : '新增材料'"
      width="50%"
    >
      <el-form :model="materialForm" label-width="100px" :rules="materialRules" ref="materialFormRef">
        <el-form-item label="名称:" prop="name">
          <el-input v-model="materialForm.name" placeholder="请输入材料名称"></el-input>
        </el-form-item>
        <el-form-item label="描述:">
          <el-input v-model="materialForm.description" type="textarea" rows="3" placeholder="请输入材料描述"></el-input>
        </el-form-item>
        <el-form-item label="SKU:" prop="sku">
          <el-input v-model="materialForm.sku" placeholder="请输入SKU"></el-input>
        </el-form-item>
        <el-form-item label="单价:" prop="unitPrice">
          <el-input-number
            v-model="materialForm.unitPrice"
            :precision="2"
            :min="0.01"
            :step="0.5"
            style="width: 200px"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="库存数量:" prop="stockQuantity">
          <el-input-number
            v-model="materialForm.stockQuantity"
            :min="0"
            :step="1"
            style="width: 200px"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="单位:" prop="unit">
          <el-input v-model="materialForm.unit" placeholder="请输入单位 (如个、升、千克)"></el-input>
        </el-form-item>
        <el-form-item label="类别:" prop="category">
          <el-select v-model="materialForm.category" placeholder="请选择类别" filterable allow-create>
            <el-option v-for="category in categories" :key="category" :label="category" :value="category"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="供应商:">
          <el-input v-model="materialForm.supplier" placeholder="请输入供应商"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="materialDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveMaterial">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 调整库存对话框 -->
    <el-dialog v-model="stockDialogVisible" title="调整库存" width="30%">
      <el-form :model="stockForm" label-width="120px">
        <el-form-item label="当前库存:">
          <span>{{ currentMaterial ? currentMaterial.stockQuantity + ' ' + currentMaterial.unit : '' }}</span>
        </el-form-item>
        <el-form-item label="调整类型:">
          <el-radio-group v-model="stockForm.adjustType">
            <el-radio value="set">设置库存</el-radio>
            <el-radio value="increase">增加库存</el-radio>
            <el-radio value="decrease">减少库存</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="数量:">
          <el-input-number
            v-model="stockForm.quantity"
            :min="stockForm.adjustType === 'decrease' ? 1 : 0"
            :max="stockForm.adjustType === 'decrease' ? (currentMaterial ? currentMaterial.stockQuantity : 9999) : 9999"
            :step="1"
          ></el-input-number>
          <span v-if="currentMaterial" class="unit-label">{{ currentMaterial.unit }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="stockDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="adjustStock">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { Search } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getAllMaterials,
  getMaterialsByCategory,
  getLowStockMaterials,
  searchMaterials,
  createMaterial,
  updateMaterial,
  updateStock,
  increaseStock,
  decreaseStock,
  deleteMaterial as apiDeleteMaterial
} from '@/api/material';

export default {
  name: 'MaterialManagement',
  components: {
    Search
  },
  
  setup() {
    // 列表数据和分页
    const materials = ref([]);
    const loading = ref(false);
    const currentPage = ref(1);
    const pageSize = ref(10);
    
    // 搜索和筛选
    const searchKeyword = ref('');
    const categoryFilter = ref('');
    const showLowStock = ref(false);
    
    // 新增/编辑对话框
    const materialDialogVisible = ref(false);
    const isEditing = ref(false);
    const materialFormRef = ref(null);
    const materialForm = reactive({
      materialId: null,
      name: '',
      description: '',
      sku: '',
      unitPrice: 0,
      stockQuantity: 0,
      supplier: '',
      category: '',
      unit: ''
    });
    
    const materialRules = {
      name: [{ required: true, message: '请输入材料名称', trigger: 'blur' }],
      sku: [{ required: true, message: '请输入SKU', trigger: 'blur' }],
      unitPrice: [{ required: true, message: '请输入单价', trigger: 'blur' }],
      stockQuantity: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
      unit: [{ required: true, message: '请输入单位', trigger: 'blur' }],
      category: [{ required: true, message: '请选择类别', trigger: 'change' }]
    };
    
    // 调整库存对话框
    const stockDialogVisible = ref(false);
    const currentMaterial = ref(null);
    const stockForm = reactive({
      adjustType: 'set',
      quantity: 0
    });
    
    // 加载所有材料
    const loadMaterials = async () => {
      loading.value = true;
      try {
        const response = await getAllMaterials();
        materials.value = response.data;
      } catch (error) {
        console.error('加载材料失败:', error);
        ElMessage.error('加载材料失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 加载过滤后的材料
    const loadFilteredMaterials = async () => {
      loading.value = true;
      try {
        if (showLowStock.value) {
          const response = await getLowStockMaterials(10);
          materials.value = response.data;
        } else if (categoryFilter.value) {
          const response = await getMaterialsByCategory(categoryFilter.value);
          materials.value = response.data;
        } else if (searchKeyword.value) {
          const response = await searchMaterials(searchKeyword.value);
          materials.value = response.data;
        } else {
          await loadMaterials();
        }
      } catch (error) {
        console.error('加载材料失败:', error);
        ElMessage.error('加载材料失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 处理搜索
    const handleSearch = () => {
      currentPage.value = 1;
      loadFilteredMaterials();
    };
    
    // 处理筛选
    const handleFilter = () => {
      currentPage.value = 1;
      loadFilteredMaterials();
    };
    
    // 处理分页大小变化
    const handleSizeChange = (size) => {
      pageSize.value = size;
    };
    
    // 处理页码变化
    const handleCurrentChange = (page) => {
      currentPage.value = page;
    };
    
    // 显示新增材料对话框
    const showAddMaterialDialog = () => {
      isEditing.value = false;
      Object.keys(materialForm).forEach(key => {
        materialForm[key] = key === 'unitPrice' || key === 'stockQuantity' ? 0 : '';
      });
      materialDialogVisible.value = true;
    };
    
    // 显示编辑材料对话框
    const editMaterial = (material) => {
      isEditing.value = true;
      Object.keys(materialForm).forEach(key => {
        materialForm[key] = material[key];
      });
      materialDialogVisible.value = true;
    };
    
    // 保存材料信息
    const saveMaterial = async () => {
      if (!materialFormRef.value) return;
      
      await materialFormRef.value.validate(async (valid) => {
        if (!valid) return;
        
        try {
          if (isEditing.value) {
            await updateMaterial(materialForm.materialId, materialForm);
            ElMessage.success('材料信息已更新');
          } else {
            await createMaterial(materialForm);
            ElMessage.success('材料已添加');
          }
          
          materialDialogVisible.value = false;
          loadMaterials();
        } catch (error) {
          console.error('保存材料失败:', error);
          ElMessage.error('保存材料失败');
        }
      });
    };
    
    // 显示调整库存对话框
    const showStockDialog = (material) => {
      currentMaterial.value = material;
      stockForm.adjustType = 'set';
      stockForm.quantity = material.stockQuantity;
      stockDialogVisible.value = true;
    };
    
    // 调整库存
    const adjustStock = async () => {
      if (!currentMaterial.value) return;
      
      try {
        switch (stockForm.adjustType) {
          case 'set':
            await updateStock(currentMaterial.value.materialId, stockForm.quantity);
            ElMessage.success('库存已更新');
            break;
          case 'increase':
            await increaseStock(currentMaterial.value.materialId, stockForm.quantity);
            ElMessage.success('库存已增加');
            break;
          case 'decrease':
            await decreaseStock(currentMaterial.value.materialId, stockForm.quantity);
            ElMessage.success('库存已减少');
            break;
        }
        
        stockDialogVisible.value = false;
        loadMaterials();
      } catch (error) {
        console.error('调整库存失败:', error);
        ElMessage.error('调整库存失败');
      }
    };
    
    // 删除材料
    const deleteMaterial = (material) => {
      ElMessageBox.confirm(`确定要删除材料 "${material.name}" 吗?`, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await apiDeleteMaterial(material.materialId);
          ElMessage.success('材料已删除');
          loadMaterials();
        } catch (error) {
          console.error('删除材料失败:', error);
          ElMessage.error('删除材料失败');
        }
      }).catch(() => {});
    };
    
    // 计算属性：分页后的材料列表
    const filteredMaterials = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return materials.value.slice(start, end);
    });
    
    // 计算属性：所有类别
    const categories = computed(() => {
      const categorySet = new Set(materials.value.map(m => m.category).filter(Boolean));
      return Array.from(categorySet);
    });
    
    onMounted(() => {
      loadMaterials();
    });
    
    return {
      materials,
      loading,
      currentPage,
      pageSize,
      searchKeyword,
      categoryFilter,
      showLowStock,
      materialDialogVisible,
      isEditing,
      materialFormRef,
      materialForm,
      materialRules,
      stockDialogVisible,
      currentMaterial,
      stockForm,
      filteredMaterials,
      categories,
      loadMaterials,
      handleSearch,
      handleFilter,
      handleSizeChange,
      handleCurrentChange,
      showAddMaterialDialog,
      editMaterial,
      saveMaterial,
      showStockDialog,
      adjustStock,
      deleteMaterial
    };
  }
};
</script>

<style scoped>
.material-management {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-area {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.search-input {
  width: 250px;
}

.low-stock {
  color: #f56c6c;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.unit-label {
  margin-left: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 