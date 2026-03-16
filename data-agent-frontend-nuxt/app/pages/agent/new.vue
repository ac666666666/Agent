<template>
  <div class="agent-create-page pa-6">
    <div class="create-form-wrapper mx-auto" style="max-width: 900px">
      <!-- Header -->
      <div class="mb-6">
        <h2 class="text-h4 font-weight-bold text-grey-darken-3 mb-2">
          创建智能体
        </h2>
        <p class="text-subtitle-1 text-grey-darken-1">
          配置您的专属数据分析智能体，让AI帮助您更好地理解和分析数据
        </p>
      </div>

      <v-card class="rounded-lg pa-6" elevation="1">
        <!-- Avatar Section -->
        <div class="mb-8">
          <label
            class="text-subtitle-1 font-weight-medium d-block mb-3 text-grey-darken-3"
            >头像设置</label
          >
          <div
            class="d-flex flex-column flex-sm-row gap-6 align-center align-sm-start"
          >
            <v-avatar size="100" class="border">
              <v-img
                v-if="agentForm.avatar"
                :src="agentForm.avatar"
                alt="Avatar"
                cover
                @error="handleImageError"
              ></v-img>
            </v-avatar>

            <div class="d-flex flex-column gap-3">
              <div class="d-flex gap-3">
                <v-btn
                  variant="outlined"
                  color="primary"
                  prepend-icon="mdi-refresh"
                  @click="regenerateAvatar"
                >
                  重新生成
                </v-btn>
                <v-btn
                  color="primary"
                  prepend-icon="mdi-upload"
                  :loading="uploading"
                  @click="triggerFileUpload"
                >
                  上传图片
                </v-btn>
              </div>
              <div class="text-caption text-grey">
                支持 JPG, PNG 格式，大小不超过 5MB
              </div>
              <input
                ref="fileInput"
                type="file"
                accept="image/*"
                style="display: none"
                @change="handleFileUpload"
              />
            </div>
          </div>
        </div>

        <v-divider class="mb-8"></v-divider>

        <!-- Form Fields -->
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="agentForm.name"
              label="智能体名称"
              placeholder="请输入智能体名称"
              variant="outlined"
              :rules="[(v) => !!v || '名称不能为空']"
              required
            ></v-text-field>
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="agentForm.category"
              label="分类"
              placeholder="请输入智能体分类"
              variant="outlined"
              :rules="[(v) => !!v || '分类不能为空']"
              required
            ></v-text-field>
          </v-col>

          <v-col cols="12">
            <v-textarea
              v-model="agentForm.description"
              label="描述"
              placeholder="请输入智能体描述"
              variant="outlined"
              rows="3"
              auto-grow
            ></v-textarea>
          </v-col>

          <v-col cols="12">
            <v-textarea
              v-model="agentForm.prompt"
              label="智能体 Prompt"
              placeholder="请输入智能体 Prompt"
              variant="outlined"
              rows="5"
              auto-grow
              hint="设定智能体的角色和行为准则"
              persistent-hint
            ></v-textarea>
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field
              v-model="agentForm.tags"
              label="标签"
              placeholder="多个标签用逗号分隔"
              variant="outlined"
              :rules="[(v) => !!v || '标签不能为空']"
              required
            ></v-text-field>
          </v-col>

          <v-col cols="12" md="6">
            <v-select
              v-model="agentForm.status"
              label="状态"
              :items="statusOptions"
              item-title="label"
              item-value="value"
              variant="outlined"
            ></v-select>
          </v-col>

          <v-col cols="12">
            <v-switch
              v-model="agentForm.humanReviewEnabled"
              label="开启人工审核"
              color="primary"
              hide-details
              inset
            ></v-switch>
            <div class="text-caption text-grey mt-1">
              开启后，智能体的回复需要人工审核通过后才会显示
            </div>
          </v-col>
        </v-row>

        <!-- Actions -->
        <div class="d-flex justify-end gap-3 mt-8">
          <v-btn variant="text" size="large" @click="goBack"> 取消 </v-btn>
          <v-btn
            color="primary"
            size="large"
            :loading="loading"
            @click="createAgent"
          >
            创建智能体
          </v-btn>
        </div>
      </v-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import { useRouter } from "vue-router";
import agentService from "~/services/agent";
import { fileUploadApi } from "~/services/fileUpload";

const router = useRouter();
const loading = ref(false);
const uploading = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);

const statusOptions = [
  { label: "待发布", value: "draft" },
  { label: "已发布", value: "published" },
  { label: "已下线", value: "offline" },
];

const agentForm = reactive({
  name: "",
  description: "",
  avatar: "",
  category: "",
  tags: "",
  prompt: "",
  status: "draft",
  humanReviewEnabled: false,
});

// Helper Functions
const generateFallbackAvatar = (): string => {
  const colors = [
    "3B82F6",
    "8B5CF6",
    "10B981",
    "F59E0B",
    "EF4444",
    "6366F1",
    "EC4899",
    "14B8A6",
  ];
  const randomColor = colors[Math.floor(Math.random() * colors.length)];
  const letters = ["AI", "数据", "智能", "DA", "BI", "ML", "DL", "NL"];
  const randomLetter = letters[Math.floor(Math.random() * letters.length)];

  const svg = `<svg width="200" height="200" xmlns="http://www.w3.org/2000/svg">
    <rect width="200" height="200" fill="#${randomColor}"/>
    <text x="100" y="120" font-family="Arial, sans-serif" font-size="48" font-weight="bold" text-anchor="middle" fill="white">${randomLetter}</text>
  </svg>`;

  return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svg)}`;
};

// Lifecycle
onMounted(() => {
  agentForm.avatar = generateFallbackAvatar();
});

// Methods
const goBack = () => {
  router.push("/agents");
};

const regenerateAvatar = () => {
  agentForm.avatar = generateFallbackAvatar();
};

const triggerFileUpload = () => {
  fileInput.value?.click();
};

const handleFileUpload = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  if (!file.type.startsWith("image/")) {
    // TODO: Toast error '请选择图片文件'
    console.error("请选择图片文件");
    return;
  }

  if (file.size > 5 * 1024 * 1024) {
    // TODO: Toast error '图片大小不能超过5MB'
    console.error("图片大小不能超过5MB");
    return;
  }

  try {
    uploading.value = true;

    // Preview immediately
    const reader = new FileReader();
    reader.onload = (e) => {
      if (e.target?.result) {
        agentForm.avatar = e.target.result as string;
      }
    };
    reader.readAsDataURL(file);

    const response = await fileUploadApi.uploadAvatar(file);
    if (response.success && response.url) {
      agentForm.avatar = response.url;
      // TODO: Toast success '头像上传成功'
    } else {
      throw new Error(response.message || "上传失败");
    }
  } catch (error) {
    console.error("头像上传失败:", error);
    agentForm.avatar = generateFallbackAvatar();
    // TODO: Toast error
  } finally {
    uploading.value = false;
    if (fileInput.value) fileInput.value.value = "";
  }
};

const handleImageError = () => {
  agentForm.avatar = generateFallbackAvatar();
};

const createAgent = async () => {
  if (
    !agentForm.name.trim() ||
    !agentForm.category.trim() ||
    !agentForm.tags.trim()
  ) {
    // TODO: Toast error '请填写必要的字段！'
    console.error("请填写必要的字段！");
    return;
  }

  try {
    loading.value = true;

    const agentData = {
      name: agentForm.name.trim(),
      description: agentForm.description.trim(),
      avatar: agentForm.avatar.trim(),
      category: agentForm.category.trim(),
      tags: agentForm.tags.trim(),
      prompt: agentForm.prompt.trim(),
      status: agentForm.status,
      humanReviewEnabled: agentForm.humanReviewEnabled ? 1 : 0,
    };

    const result = await agentService.create(agentData);
    // TODO: Toast success
    router.push(`/agent/${result.id}`);
  } catch (error) {
    console.error("创建智能体失败:", error);
    // TODO: Toast error
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.agent-create-page {
  background-color: #f8fafc;
  min-height: 100%;
}

.gap-3 {
  gap: 12px;
}
.gap-6 {
  gap: 24px;
}
</style>